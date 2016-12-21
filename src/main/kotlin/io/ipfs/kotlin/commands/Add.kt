package io.ipfs.kotlin.commands

import com.squareup.moshi.JsonAdapter
import io.ipfs.kotlin.IPFSConnection
import io.ipfs.kotlin.model.NamedHash
import okhttp3.*
import java.io.File
import java.net.URLEncoder

class Add(val ipfs: IPFSConnection) {

    private val adapter: JsonAdapter<NamedHash> by lazy { ipfs.moshi.adapter(NamedHash::class.java) }

    @JvmOverloads fun file(file: File, name: String = "file", filename: String = name): NamedHash {

        return addGeneric {
            addFile(it, file, name, filename)
        }

    }

    // this has to be outside the lambda because it is reentrant to handle subdirectory structures
    private fun addFile(builder: MultipartBody.Builder, file: File, name: String, filename: String) {

        if (file.isDirectory) {
            // add directory
            val body = RequestBody.create(MediaType.parse("Content-Disposition: file;"
                    + " filename=\"" + URLEncoder.encode(file.name, "UTF-8") + "\";"
                    + " Content-Type: application/x-directory;"
                    + " Content-Transfer-Encoding: binary"),file.name)
            builder.addFormDataPart(name, filename, body)
            // add files and subdirectories
            for (f: File in file.listFiles()) {
                addFile(builder, f, f.name, f.name)
            }
        } else {
            val body = RequestBody.create(MediaType.parse("application/octet-stream"), file)
            builder.addFormDataPart(name, filename, body)
        }

    }

    @JvmOverloads fun string(text: String, name: String = "string", filename: String = name): NamedHash {

        return addGeneric {
            val body = RequestBody.create(MediaType.parse("application/octet-stream"), text)
            it.addFormDataPart(name, filename, body)
        }

    }

    private fun addGeneric(withBuilder: (MultipartBody.Builder) -> Any): NamedHash {

        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        withBuilder(builder)
        val requestBody = builder.build();

        val request = Request.Builder()
                .url("${ipfs.base_url}add?stream-channels=true&progress=false")
                .post(requestBody)
                .build();

        val response = ipfs.okHttpClient.newCall(request).execute().body()
        val result = adapter.fromJson(response.source())
        response.close()
        return result

    }
}
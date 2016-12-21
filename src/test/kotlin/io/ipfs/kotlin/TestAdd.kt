package io.ipfs.kotlin

import okhttp3.mockwebserver.MockResponse
import org.junit.Test
import org.assertj.core.api.Assertions.*;
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class TestAdd() :BaseIPFSWebserverTest() {

    @Test
    fun testAddString() {
        // setup
        server.enqueue(MockResponse().setBody("{\"Hash\":\"hashprobe\",\"Name\":\"nameprobe\"}"));

        // invoke
        val addString = ipfs.add.string("foo")

        // assert
        assertThat(addString.Hash).isEqualTo("hashprobe")
        assertThat(addString.Name).isEqualTo("nameprobe")

        val executedRequest = server.takeRequest();
        assertThat(executedRequest.path).startsWith("/add");

    }

    @Test
    fun testAddFile() {
        // setup
        server.enqueue(MockResponse().setBody("{\"Hash\":\"hashprobe\",\"Name\":\"nameprobe\"}"));

        // invoke
        val addString = ipfs.add.file(File.createTempFile("temptestfile", null))

        // assert
        assertThat(addString.Hash).isEqualTo("hashprobe")
        assertThat(addString.Name).isEqualTo("nameprobe")

        val executedRequest = server.takeRequest();
        assertThat(executedRequest.path).startsWith("/add");

    }

    @Test
    fun testAddDirectory() {
        // setup
        server.enqueue(MockResponse().setBody("{\"Hash\":\"hashprobe\",\"Name\":\"nameprobe\"}"));

        // create nested subdirectories
        val path = Files.createTempDirectory("temptestdir")
        File.createTempFile("dirtemptestfile",null,path.toFile())
        val subdirpath = Files.createDirectory(Paths.get(path.toString()+File.separator+"subdir"))
        File.createTempFile("subdirtemptestfile",null,subdirpath.toFile())
        File.createTempFile("dirtemptestfile2",null, path.toFile())

        val addString = ipfs.add.file(path.toFile())

        // assert
        assertThat(addString.Hash).isEqualTo("hashprobe")
        assertThat(addString.Name).isEqualTo("nameprobe")

        val executedRequest = server.takeRequest();
        assertThat(executedRequest.path).startsWith("/add");

    }


}
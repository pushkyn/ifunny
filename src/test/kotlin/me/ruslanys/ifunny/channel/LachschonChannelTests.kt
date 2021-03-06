package me.ruslanys.ifunny.channel

import kotlinx.coroutines.runBlocking
import me.ruslanys.ifunny.util.readResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class LachschonChannelTests {

    private val channel = LachschonChannel()

    @Test
    fun firstPagePathTest() = runBlocking<Unit> {
        val pagePath = channel.pagePath(1)
        assertThat(pagePath).isEqualTo("https://www.lachschon.de/?set_gallery_type=image&page=1")
    }

    @Test
    fun hundredPagePathTest() = runBlocking<Unit> {
        val pagePath = channel.pagePath(100)
        assertThat(pagePath).isEqualTo("https://www.lachschon.de/?set_gallery_type=image&page=100")
    }

    @Test
    fun parseProperPageShouldReturnList() = runBlocking<Unit> {
        val html = readResource<LachschonChannelTests>("lachschon/page.html")

        // --
        val page = channel.parsePage(1, html)
        val list = page.memesInfo

        // --
        assertThat(page.hasNext).isTrue()
        assertThat(list).hasSize(10)
        assertThat(list).allMatch { it.pageUrl != null }
        assertThat(list).allMatch { it.title != null }
        assertThat(list).allMatch { it.author != null }
        assertThat(list).allMatch { it.likes != null }
        assertThat(list).allMatch { it.comments != null }
    }

    @Test
    fun parseLastPageShouldReturnHasNextFalse() = runBlocking<Unit> {
        val html = readResource<LachschonChannelTests>("lachschon/page_last.html")

        // --
        val page = channel.parsePage(1, html)

        // --
        assertThat(page.hasNext).isFalse()
    }

    @Test
    fun parseInvalidPageShouldReturnEmptyList() = runBlocking {
        val page = channel.parsePage(1, "<html></html>")
        assertThat(page.hasNext).isFalse()
        assertThat(page.memesInfo).isEmpty()
    }

    @Test
    fun parsePictureMeme() = runBlocking<Unit> {
        val baseInfo = MemeInfo(
                pageUrl = "https://www.lachschon.de/item/226337-Fastrichtig/",
                title = "Fast richtig",
                author = "panic",
                likes = 0,
                comments = 12
        )
        val html = readResource<LachschonChannelTests>("lachschon/meme.html")

        // --
        val info = channel.parseMeme(baseInfo, html)

        // --
        assertThat(info.pageUrl).isEqualTo(baseInfo.pageUrl)
        assertThat(info.title).isEqualTo(baseInfo.title)
        assertThat(info.author).isEqualTo(baseInfo.author)
        assertThat(info.likes).isEqualTo(baseInfo.likes)
        assertThat(info.comments).isEqualTo(baseInfo.comments)

        assertThat(info.originUrl).isEqualTo("https://img01.lachschon.de/images/226337_Fastrichtig_FuZCOZm.jpg")
        assertThat(info.publishDateTime).isEqualTo(LocalDateTime.of(2020, 1, 3, 12, 55, 8))
    }

}

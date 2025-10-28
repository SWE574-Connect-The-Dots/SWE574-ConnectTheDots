package com.yybb.myapplication.presentation.ui.viewmodel

import com.yybb.myapplication.data.network.dto.CreateSpaceResponse
import com.yybb.myapplication.data.network.dto.TagDto
import com.yybb.myapplication.data.repository.SpacesRepository
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CreateSpaceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: CreateSpaceViewModel
    private lateinit var mockSpacesRepository: SpacesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockSpacesRepository = mock()
        whenever(mockSpacesRepository.isColorBlindTheme).thenReturn(flowOf(false))
        viewModel = CreateSpaceViewModel(mockSpacesRepository)
    }

    @Test
    fun `getTags should call repository and emit Success state`() = runTest {
        val query = "artificial intelligence"
        whenever(mockSpacesRepository.getWikiTags(query)).thenReturn(Result.success(emptyList()))

        viewModel.getTags(query)
        advanceUntilIdle()
        
        val state = viewModel.tagWikidataUiState.value
        TestCase.assertTrue(state is RetrieveTagWikidataUiState.Success)
        verify(mockSpacesRepository).getWikiTags(query)
    }

    @Test
    fun `getTags should emit Success state with tags on successful search`() = runTest {
        val query = "machine learning"
        val mockTags = listOf(
            TagDto("Q2539", "Machine learning", "Study of algorithms that improve through experience", "https://www.wikidata.org/wiki/Q2539"),
            TagDto("Q11660", "Artificial intelligence", "Intelligence demonstrated by machines", "https://www.wikidata.org/wiki/Q11660"),
            TagDto("Q475010", "Deep learning", "Machine learning method based on artificial neural networks", "https://www.wikidata.org/wiki/Q475010")
        )
        whenever(mockSpacesRepository.getWikiTags(query)).thenReturn(Result.success(mockTags))

        viewModel.getTags(query)
        advanceUntilIdle()

        val state = viewModel.tagWikidataUiState.value
        TestCase.assertTrue(state is RetrieveTagWikidataUiState.Success)
        val successState = state as RetrieveTagWikidataUiState.Success
        TestCase.assertEquals(mockTags, successState.tags)
        TestCase.assertTrue(successState.showResults)
    }

    @Test
    fun `getTags should emit Error state on repository failure`() = runTest {
        val query = "quantum computing"
        val errorMessage = "Network connection failed"
        whenever(mockSpacesRepository.getWikiTags(query)).thenReturn(Result.failure(Exception(errorMessage)))

        viewModel.getTags(query)
        advanceUntilIdle()

        val state = viewModel.tagWikidataUiState.value
        TestCase.assertTrue(state is RetrieveTagWikidataUiState.Error)
        TestCase.assertEquals(errorMessage, (state as RetrieveTagWikidataUiState.Error).message)
    }

    @Test
    fun `getTags should emit Error state with default message on null exception message`() = runTest {
        val query = "blockchain technology"
        whenever(mockSpacesRepository.getWikiTags(query)).thenReturn(Result.failure(Exception()))

        viewModel.getTags(query)
        advanceUntilIdle()

        val state = viewModel.tagWikidataUiState.value
        TestCase.assertTrue(state is RetrieveTagWikidataUiState.Error)
        TestCase.assertEquals("An unknown error occurred", (state as RetrieveTagWikidataUiState.Error).message)
    }

    @Test
    fun `createSpace should call repository and emit Success state`() = runTest {
        val formState = CreateSpaceFormState(
            spaceTitle = "The Future of AI in Healthcare",
            spaceDescription = "Exploring how artificial intelligence is revolutionizing medical diagnosis, treatment planning, and patient care. This space discusses the latest breakthroughs in AI-powered medical technologies and their potential impact on healthcare delivery.",
            selectedTags = emptyList()
        )
        whenever(mockSpacesRepository.createSpace(any(), any(), any())).thenReturn(Result.success(CreateSpaceResponse(1, "The Future of AI in Healthcare", "Exploring how artificial intelligence is revolutionizing medical diagnosis, treatment planning, and patient care.", null, "2023-12-01T10:30:00Z")))

        viewModel.createSpace(formState)
        advanceUntilIdle()

        val state = viewModel.createSpaceUiState.value
        TestCase.assertTrue(state is CreateSpaceUiState.Success)
        verify(mockSpacesRepository).createSpace("The Future of AI in Healthcare", "Exploring how artificial intelligence is revolutionizing medical diagnosis, treatment planning, and patient care. This space discusses the latest breakthroughs in AI-powered medical technologies and their potential impact on healthcare delivery.", emptyList())
    }

    @Test
    fun `createSpace should emit Error state on repository failure`() = runTest {
        val formState = CreateSpaceFormState(
            spaceTitle = "Sustainable Energy Solutions",
            spaceDescription = "A comprehensive discussion on renewable energy technologies, their implementation challenges, and environmental impact.",
            selectedTags = emptyList()
        )
        val errorMessage = "Failed to create space: Server unavailable"
        whenever(mockSpacesRepository.createSpace(any(), any(), any())).thenReturn(Result.failure(Exception(errorMessage)))

        viewModel.createSpace(formState)
        advanceUntilIdle()

        val state = viewModel.createSpaceUiState.value
        TestCase.assertTrue(state is CreateSpaceUiState.Error)
        TestCase.assertEquals(errorMessage, (state as CreateSpaceUiState.Error).message)
    }

    @Test
    fun `createSpace should call repository with correct parameters`() = runTest {
        val formState = CreateSpaceFormState(
            spaceTitle = "Climate Change Research",
            spaceDescription = "Investigating the latest findings in climate science and their implications for global policy.",
            selectedTags = listOf(TagDto("Q7942", "Climate change", "Long-term change in global or regional climate patterns", "https://www.wikidata.org/wiki/Q7942"))
        )
        whenever(mockSpacesRepository.createSpace(any(), any(), any())).thenReturn(Result.success(CreateSpaceResponse(1, "Climate Change Research", "Investigating the latest findings in climate science and their implications for global policy.", null, "2023-12-01T14:20:00Z")))

        viewModel.createSpace(formState)
        advanceUntilIdle()

        verify(mockSpacesRepository).createSpace("Climate Change Research", "Investigating the latest findings in climate science and their implications for global policy.", formState.selectedTags)
    }

    @Test
    fun `createSpace should reset form state on successful creation`() = runTest {
        val formState = CreateSpaceFormState(
            spaceTitle = "Space Exploration Technologies",
            spaceDescription = "Discussing the latest developments in rocket technology, satellite systems, and interplanetary missions.",
            selectedTags = listOf(TagDto("Q40218", "Space exploration", "The investigation of outer space by means of manned and unmanned spacecraft", "https://www.wikidata.org/wiki/Q40218"))
        )
        whenever(mockSpacesRepository.createSpace(any(), any(), any())).thenReturn(Result.success(CreateSpaceResponse(1, "Space Exploration Technologies", "Discussing the latest developments in rocket technology, satellite systems, and interplanetary missions.", null, "2023-12-01T16:45:00Z")))

        viewModel.updateFormState(formState)
        TestCase.assertEquals(formState, viewModel.formState.value)

        viewModel.createSpace(formState)
        advanceUntilIdle()

        val resetFormState = viewModel.formState.value
        TestCase.assertEquals("", resetFormState.spaceTitle)
        TestCase.assertEquals("", resetFormState.spaceDescription)
        TestCase.assertTrue(resetFormState.selectedTags.isEmpty())
    }

    @Test
    fun `updateFormState should update form state correctly`() = runTest {
        val newFormState = CreateSpaceFormState(
            spaceTitle = "Digital Privacy and Security",
            spaceDescription = "Exploring the challenges and solutions in protecting personal data in the digital age.",
            selectedTags = listOf(TagDto("Q1914636", "Privacy", "Ability of an individual or group to seclude themselves or information about themselves", "https://www.wikidata.org/wiki/Q1914636")),
            showTagSearch = true,
            tagSearchQuery = "cybersecurity",
            showSearchResults = true
        )

        viewModel.updateFormState(newFormState)

        TestCase.assertEquals(newFormState, viewModel.formState.value)
    }

    @Test
    fun `updateFormState should reset search when hiding tag search`() = runTest {
        val initialState = CreateSpaceFormState(
            showTagSearch = true,
            tagSearchQuery = "renewable energy",
            showSearchResults = true
        )
        viewModel.updateFormState(initialState)

        val newState = initialState.copy(showTagSearch = false)
        viewModel.updateFormState(newState)

        val finalState = viewModel.formState.value
        TestCase.assertFalse(finalState.showTagSearch)
        TestCase.assertEquals("", finalState.tagSearchQuery)
        TestCase.assertFalse(finalState.showSearchResults)
    }

    @Test
    fun `resetFormState should reset form to initial state`() = runTest {
        val formState = CreateSpaceFormState(
            spaceTitle = "Biotechnology Breakthroughs",
            spaceDescription = "Latest advances in genetic engineering, synthetic biology, and medical biotechnology applications.",
            selectedTags = listOf(TagDto("Q7108", "Biotechnology", "Application of biological systems, living organisms, or derivatives thereof, to make or modify products or processes", "https://www.wikidata.org/wiki/Q7108")),
            showTagSearch = true,
            tagSearchQuery = "genetic engineering",
            showSearchResults = true
        )
        viewModel.updateFormState(formState)

        viewModel.resetFormState()

        val resetState = viewModel.formState.value
        TestCase.assertEquals("", resetState.spaceTitle)
        TestCase.assertEquals("", resetState.spaceDescription)
        TestCase.assertTrue(resetState.selectedTags.isEmpty())
        TestCase.assertFalse(resetState.showTagSearch)
        TestCase.assertEquals("", resetState.tagSearchQuery)
        TestCase.assertFalse(resetState.showSearchResults)
    }

    @Test
    fun `resetTagWikidataState should reset tag state to Initial`() = runTest {
        whenever(mockSpacesRepository.getWikiTags(any())).thenReturn(Result.failure(Exception("Error")))
        viewModel.getTags("test")
        advanceUntilIdle()

        TestCase.assertTrue(viewModel.tagWikidataUiState.value is RetrieveTagWikidataUiState.Error)

        viewModel.resetTagWikidataState()

        TestCase.assertTrue(viewModel.tagWikidataUiState.value is RetrieveTagWikidataUiState.Initial)
    }

    @Test
    fun `resetCreateSpaceState should reset create space state to Initial`() = runTest {
        whenever(mockSpacesRepository.createSpace(any(), any(), any())).thenReturn(Result.failure(Exception("Error")))
        viewModel.createSpace(CreateSpaceFormState())
        advanceUntilIdle()

        TestCase.assertTrue(viewModel.createSpaceUiState.value is CreateSpaceUiState.Error)

        viewModel.resetCreateSpaceState()

        TestCase.assertTrue(viewModel.createSpaceUiState.value is CreateSpaceUiState.Initial)
    }

    @Test
    fun `CreateSpaceFormState isFormValid should return true for valid form`() = runTest {
        val validFormState = CreateSpaceFormState(
            spaceTitle = "Quantum Computing Applications",
            spaceDescription = "Exploring practical applications of quantum computing in cryptography, optimization, and scientific simulation."
        )

        TestCase.assertTrue(validFormState.isFormValid)
    }

    @Test
    fun `CreateSpaceFormState isFormValid should return false for empty title`() = runTest {
        val invalidFormState = CreateSpaceFormState(
            spaceTitle = "",
            spaceDescription = "A comprehensive analysis of renewable energy technologies and their environmental impact."
        )

        TestCase.assertFalse(invalidFormState.isFormValid)
    }

    @Test
    fun `CreateSpaceFormState isFormValid should return false for empty description`() = runTest {
        val invalidFormState = CreateSpaceFormState(
            spaceTitle = "Neuroscience Research",
            spaceDescription = ""
        )

        TestCase.assertFalse(invalidFormState.isFormValid)
    }

    @Test
    fun `CreateSpaceFormState isFormValid should return false for blank title`() = runTest {
        val invalidFormState = CreateSpaceFormState(
            spaceTitle = "   ",
            spaceDescription = "Discussion on the latest developments in autonomous vehicle technology and safety systems."
        )

        TestCase.assertFalse(invalidFormState.isFormValid)
    }

    @Test
    fun `CreateSpaceFormState isFormValid should return false for blank description`() = runTest {
        val invalidFormState = CreateSpaceFormState(
            spaceTitle = "Urban Planning Innovations",
            spaceDescription = "   "
        )

        TestCase.assertFalse(invalidFormState.isFormValid)
    }

    @Test
    fun `CreateSpaceFormState isSearchEnabled should return true for non-blank query`() = runTest {
        val formState = CreateSpaceFormState(tagSearchQuery = "sustainable technology")

        TestCase.assertTrue(formState.isSearchEnabled)
    }

    @Test
    fun `CreateSpaceFormState isSearchEnabled should return false for blank query`() = runTest {
        val formState = CreateSpaceFormState(tagSearchQuery = "   ")

        TestCase.assertFalse(formState.isSearchEnabled)
    }

    @Test
    fun `CreateSpaceFormState isSearchEnabled should return false for empty query`() = runTest {
        val formState = CreateSpaceFormState(tagSearchQuery = "")

        TestCase.assertFalse(formState.isSearchEnabled)
    }

    @Test
    fun `initial tag wikidata state should be Initial`() = runTest {
        TestCase.assertTrue(viewModel.tagWikidataUiState.value is RetrieveTagWikidataUiState.Initial)
    }

    @Test
    fun `initial create space state should be Initial`() = runTest {
        TestCase.assertTrue(viewModel.createSpaceUiState.value is CreateSpaceUiState.Initial)
    }

    @Test
    fun `initial form state should be empty`() = runTest {
        val initialState = viewModel.formState.value
        TestCase.assertEquals("", initialState.spaceTitle)
        TestCase.assertEquals("", initialState.spaceDescription)
        TestCase.assertTrue(initialState.selectedTags.isEmpty())
        TestCase.assertFalse(initialState.showTagSearch)
        TestCase.assertEquals("", initialState.tagSearchQuery)
        TestCase.assertFalse(initialState.showSearchResults)
    }

    @Test
    fun `getTags with empty query should still call repository`() = runTest {
        whenever(mockSpacesRepository.getWikiTags("")).thenReturn(Result.success(emptyList()))

        viewModel.getTags("")
        advanceUntilIdle()

        verify(mockSpacesRepository).getWikiTags("")
    }

    @Test
    fun `createSpace with empty tags should still call repository`() = runTest {
        val formState = CreateSpaceFormState(
            spaceTitle = "Marine Biology Research",
            spaceDescription = "Investigating marine ecosystems, biodiversity, and conservation strategies in ocean environments.",
            selectedTags = emptyList()
        )
        whenever(mockSpacesRepository.createSpace(any(), any(), any())).thenReturn(Result.success(CreateSpaceResponse(1, "Marine Biology Research", "Investigating marine ecosystems, biodiversity, and conservation strategies in ocean environments.", null, "2023-12-01T18:15:00Z")))

        viewModel.createSpace(formState)
        advanceUntilIdle()

        verify(mockSpacesRepository).createSpace("Marine Biology Research", "Investigating marine ecosystems, biodiversity, and conservation strategies in ocean environments.", emptyList())
    }

    @Test
    fun `createSpace with multiple tags should call repository with all tags`() = runTest {
        val tags = listOf(
            TagDto("Q2539", "Machine learning", "Study of algorithms that improve through experience", "https://www.wikidata.org/wiki/Q2539"),
            TagDto("Q11660", "Artificial intelligence", "Intelligence demonstrated by machines", "https://www.wikidata.org/wiki/Q11660"),
            TagDto("Q475010", "Deep learning", "Machine learning method based on artificial neural networks", "https://www.wikidata.org/wiki/Q475010")
        )
        val formState = CreateSpaceFormState(
            spaceTitle = "AI Research Collaboration",
            spaceDescription = "A collaborative space for researchers working on artificial intelligence, machine learning, and deep learning projects.",
            selectedTags = tags
        )
        whenever(mockSpacesRepository.createSpace(any(), any(), any())).thenReturn(Result.success(CreateSpaceResponse(1, "AI Research Collaboration", "A collaborative space for researchers working on artificial intelligence, machine learning, and deep learning projects.", null, "2023-12-01T20:00:00Z")))

        viewModel.createSpace(formState)
        advanceUntilIdle()
        verify(mockSpacesRepository).createSpace("AI Research Collaboration", "A collaborative space for researchers working on artificial intelligence, machine learning, and deep learning projects.", tags)
    }
}

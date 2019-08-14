/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.rest;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.service.ApplicationTestConfig;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Test class for {@link PetRestController}
 *
 * @author Vitaliy Fedoriv
 */

@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=ApplicationTestConfig.class)
@WebAppConfiguration
public class PetRestControllerTests {

    @Autowired
    private PetRestController petRestController;

    @MockBean
    protected ClinicService clinicService;

    private MockMvc mockMvc;

    private List<Pet> pets;

    List<Visit> visits = new ArrayList<>();

    @Before
    public void initPets(){
    	this.mockMvc = MockMvcBuilders.standaloneSetup(petRestController)
    			.setControllerAdvice(new ExceptionControllerAdvice())
    			.build();
    	pets = new ArrayList<Pet>();

    	Owner owner = new Owner();
    	owner.setId(1);
    	owner.setFirstName("Eduardo");
    	owner.setLastName("Rodriquez");
    	owner.setAddress("2693 Commerce St.");
    	owner.setCity("McFarland");
    	owner.setTelephone("6085558763");

    	PetType petType = new PetType();
    	petType.setId(2);
    	petType.setName("dog");

    	Pet pet = new Pet();
    	pet.setId(3);
    	pet.setName("Rosy");
    	pet.setBirthDate(new Date());
    	pet.setOwner(owner);
    	pet.setType(petType);


        pets.add(pet);

    	pet = new Pet();
    	pet.setId(4);
    	pet.setName("Jewel");
    	pet.setBirthDate(new Date());
    	pet.setOwner(owner);
    	pet.setType(petType);
    	pets.add(pet);
    }

    @After
    public void cleanup(){
        pets = null;
    }

    @Test
    public void testGetPetSuccess() throws Exception {
    	given(this.clinicService.findPetById(3)).willReturn(pets.get(0));
        this.mockMvc.perform(get("/api/pets/3")
        	.accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.id").value(3))
            .andExpect(jsonPath("$.name").value("Rosy"));
        verify( this.clinicService, times( 1 ) ).findPetById( 3);
    }

    @Test
    public void testGetPetNotFound() throws Exception {
    	given(this.clinicService.findPetById(-1)).willReturn(null);
        this.mockMvc.perform(get("/api/pets/-1")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAllPetsSuccess() throws Exception {
    	given(this.clinicService.findAllPets()).willReturn(pets);
        this.mockMvc.perform(get("/api/pets/")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.[0].id").value(3))
            .andExpect(jsonPath("$.[0].name").value("Rosy"))
            .andExpect(jsonPath("$.[1].id").value(4))
            .andExpect(jsonPath("$.[1].name").value("Jewel"));
    }

    @Test
    public void testGetAllPetsNotFound() throws Exception {
    	pets.clear();
    	given(this.clinicService.findAllPets()).willReturn(pets);
        this.mockMvc.perform(get("/api/pets/")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }


    @Test
    public void testGetPetsWithVisitsSuccess() throws Exception {
        // create visits to test get pets with visits
        Pet pet = pets.get(0);
        Visit visit = new Visit();
        visit.setId( 1 );
        visits.add( visit );
        for( Visit _visit : visits ){
            pet.addVisit( _visit );
            _visit.setPet( pet );
        }

        // we just need to verify the service and that it returns a set of pets
        given(this.clinicService.findPetsWithVisits()).willReturn(pets);
        this.mockMvc.perform(get("/api/pets/petswithvisits")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.[0].id").value(3))
            .andExpect(jsonPath("$.[0].name").value("Rosy"))
            .andExpect(jsonPath("$.[0].visits").isNotEmpty());
    }

    @Test
    public void testGetPetsWithVisitsNotFound() throws Exception {
        // we just need to verify the service and that it returns a set of pets
        given(this.clinicService.findAllPets()).willReturn( new ArrayList<>());
        this.mockMvc.perform(get("/api/pets/petswithvisits")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void testGetPetsByOwnerIdSuccess() throws Exception {
        // we just need to verify the service and that it returns a set of pets
        given(this.clinicService.findPetsByOwnerId( 1 )).willReturn(pets);
        this.mockMvc.perform(get("/api/pets/ownerid/1")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.[0].id").value(3))
            .andExpect(jsonPath("$.[0].name").value("Rosy"))
            .andExpect(jsonPath("$.[0].owner.id").value( 1 ))
            .andExpect(jsonPath("$.[1].id").value(4))
            .andExpect(jsonPath("$.[1].name").value("Jewel"))
            .andExpect(jsonPath("$.[1].owner.id").value( 1 ));
    }

    @Test
    public void testGetPetsByOwnerIdNotFound() throws Exception {
        pets.clear();
        given(this.clinicService.findPetsByOwnerId(1)).willReturn(pets);
        this.mockMvc.perform(get("/api/pets/ownerid/1")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void testCreatePetSuccess() throws Exception {
    	Pet newPet = pets.get(0);
    	newPet.setId(999);
    	ObjectMapper mapper = new ObjectMapper();
    	String newPetAsJSON = mapper.writeValueAsString(newPet);
    	this.mockMvc.perform(post("/api/pets/")
    		.content(newPetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
    		.andExpect(status().isCreated());
    }

    @Test
    public void testCreatePetError() throws Exception {
    	Pet newPet = pets.get(0);
    	newPet.setId(null);
    	newPet.setName(null);
    	ObjectMapper mapper = new ObjectMapper();
    	String newPetAsJSON = mapper.writeValueAsString(newPet);
    	this.mockMvc.perform(post("/api/pets/")
        		.content(newPetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        		.andExpect(status().isBadRequest());
     }

    @Test
    public void testUpdatePetSuccess() throws Exception {
    	given(this.clinicService.findPetById(3)).willReturn(pets.get(0));
    	Pet newPet = pets.get(0);
    	newPet.setName("Rosy I");
    	ObjectMapper mapper = new ObjectMapper();
    	String newPetAsJSON = mapper.writeValueAsString(newPet);
    	this.mockMvc.perform(put("/api/pets/3")
    		.content(newPetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(content().contentType("application/json;charset=UTF-8"))
        	.andExpect(status().isNoContent());

    	this.mockMvc.perform(get("/api/pets/3")
           	.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json;charset=UTF-8"))
            .andExpect(jsonPath("$.id").value(3))
            .andExpect(jsonPath("$.name").value("Rosy I"));

    }

    @Test
    public void testUpdatePetError() throws Exception {
    	Pet newPet = pets.get(0);
    	newPet.setName("");
    	ObjectMapper mapper = new ObjectMapper();
    	String newPetAsJSON = mapper.writeValueAsString(newPet);
    	this.mockMvc.perform(put("/api/pets/3")
    		.content(newPetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(status().isBadRequest());
     }

    @Test
    public void testDeletePetSuccess() throws Exception {
    	Pet newPet = pets.get(0);
    	ObjectMapper mapper = new ObjectMapper();
    	String newPetAsJSON = mapper.writeValueAsString(newPet);
    	given(this.clinicService.findPetById(3)).willReturn(pets.get(0));
    	this.mockMvc.perform(delete("/api/pets/3")
    		.content(newPetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(status().isNoContent());
    }

    @Test
    public void testDeletePetError() throws Exception {
    	Pet newPet = pets.get(0);
    	ObjectMapper mapper = new ObjectMapper();
    	String newPetAsJSON = mapper.writeValueAsString(newPet);
    	given(this.clinicService.findPetById(-1)).willReturn(null);
    	this.mockMvc.perform(delete("/api/pets/-1")
    		.content(newPetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(status().isNotFound());
    }

}

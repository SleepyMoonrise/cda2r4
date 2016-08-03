package tr.com.srdc.cda2fhir;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.eclipse.emf.common.util.EList;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openhealthtools.mdht.uml.cda.ClinicalDocument;
import org.openhealthtools.mdht.uml.cda.PatientRole;
import org.openhealthtools.mdht.uml.cda.consol.ContinuityOfCareDocument;
import org.openhealthtools.mdht.uml.cda.util.CDAUtil;
import org.openhealthtools.mdht.uml.hl7.datatypes.EN;
import org.openhealthtools.mdht.uml.hl7.datatypes.ENXP;
import org.openhealthtools.mdht.uml.hl7.datatypes.II;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.ExtensionDt;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.parser.IParser;
import tr.com.srdc.cda2fhir.impl.ResourceTransformerImpl;
import tr.com.srdc.cda2fhir.impl.ValueSetsTransformerImpl;

public class ResourceTransformerTestNecip {
	
	// Test one method at a time. Use annotation @Ignore for the remaining methods.
	
	// context
	private static final FhirContext myCtx = FhirContext.forDstu2();
	
	ResourceTransformerImpl rt = new ResourceTransformerImpl();
	DataTypesTransformerTest dtt = new DataTypesTransformerTest();
	ValueSetsTransformerImpl vsti = new ValueSetsTransformerImpl();
	private FileInputStream fisCDA;
	private FileInputStream fisCCD;
	private ClinicalDocument cda;
	private ContinuityOfCareDocument ccd;
	
	private void printJSON(IResource res) {
	    IParser jsonParser = myCtx.newJsonParser();
	    jsonParser.setPrettyPrint(true);
	    System.out.println(jsonParser.encodeResourceToString(res));
	}
	
	public ResourceTransformerTestNecip() {
        CDAUtil.loadPackages();
        try {
	        fisCDA = new FileInputStream("src/test/resources/SampleCDADocument.xml");
	        // fisCCD = new FileInputStream("src/test/resources/C-CDA_R2-1_CCD.xml");
	        fisCCD = new FileInputStream("src/test/resources/Vitera_CCDA_SMART_Sample.xml");
		} catch (FileNotFoundException ex) {
	        ex.printStackTrace();
	    }
        
        try {
        	if( fisCDA != null ) { 
        		cda = CDAUtil.load(fisCDA); 
        	}
        	if( fisCCD != null ) {
        		// To validate the file, use the following two lines instead of the third line
//        		ValidationResult result = new ValidationResult();
//        		ccd = (ContinuityOfCareDocument) CDAUtil.load(fisCCD,result);
        		ccd = (ContinuityOfCareDocument) CDAUtil.load(fisCCD);
        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	// Following method just prints the [FHIR]Organization in JSON form.
	// One example transformed to fhir form and there was no error.
	@Ignore
	public void testOrganization2Organization(){
		ResourceTransformerTestNecip test = new ResourceTransformerTestNecip();
		
		int patientCount = 0;
		for( org.openhealthtools.mdht.uml.cda.PatientRole patRole : test.ccd.getPatientRoles() ){
			System.out.print("PatientRole["+patientCount++ +"].");
			org.openhealthtools.mdht.uml.cda.Organization cdaOrg = patRole.getProviderOrganization();
			System.out.println( "Transformating starting..." );
			ca.uhn.fhir.model.dstu2.resource.Organization fhirOrg = rt.Organization2Organization(cdaOrg);
			System.out.println("End of transformation. Printing the resource as JSON object..");
			printJSON( fhirOrg );
			System.out.println("End of print.");
		}
	}
	
	
	// Following method just prints the [FHIR]Communication in JSON form.
	// One example transformed to fhir form and there was no error.
	@Ignore
	public void testLanguageCommunication2Communication(){
		ResourceTransformerTestNecip test = new ResourceTransformerTestNecip();
		int patientCount = 0;
		for( org.openhealthtools.mdht.uml.cda.Patient patient : test.ccd.getPatients() ){
			System.out.print("Patient["+patientCount++ +"].");
			int lcCount = 0;
			for( org.openhealthtools.mdht.uml.cda.LanguageCommunication LC : patient.getLanguageCommunications() ){
				System.out.print("LC["+ lcCount++ +"]\n");
				
				System.out.println( "Transformating starting..." );
				ca.uhn.fhir.model.dstu2.resource.Patient.Communication fhirCommunication = rt.LanguageCommunication2Communication(LC);
				System.out.println("End of transformation. Building a patient resource..");
				ca.uhn.fhir.model.dstu2.resource.Patient fhirPatient = new ca.uhn.fhir.model.dstu2.resource.Patient();
				fhirPatient.addCommunication(fhirCommunication);
				System.out.println("End of build. Printing the resource as JSON object..");
				printJSON( fhirPatient );
				System.out.println("End of print.");
			}
		}
	}
	
	
	
	
	@Test
    public void testPatientRole2Patient(){
		ResourceTransformerTestNecip test = new ResourceTransformerTestNecip();
		EList<PatientRole> patientRoles = test.ccd.getPatientRoles();

		// We traverse each of the patientRoles included in the document
		// We apply the tests for each of them
		for( PatientRole pr : patientRoles ){
			
			// here we do the transformation by calling the method rt.PatientRole2Patient
			Patient patient = rt.PatientRole2Patient(pr);
			
			// ta-ta-ta-taa!
			System.out.println("Printing the resource as JSON object..");
			printJSON( patient );
			System.out.println("End of print");
			
			
			// patient.identifier
			int idCount = 0;
			for( II id : pr.getIds() ){
				// To see the values, you can use the following print lines.
//				System.out.println( id.getRoot() );
//				System.out.println( id.getExtension() );
//				System.out.println( id.getAssigningAuthorityName() );
				
				Assert.assertEquals("pr.id.root #"+ idCount +" was not transformed",id.getRoot(),  patient.getIdentifier().get(idCount).getSystem() );
				Assert.assertEquals("pr.id.extension #"+ idCount +" was not transformed",id.getExtension(),  patient.getIdentifier().get(idCount).getValue() );
				Assert.assertEquals("pr.id.assigningAuthorityName #"+ idCount +" was not transformed",id.getAssigningAuthorityName(),  patient.getIdentifier().get(idCount).getAssigner().getReference().getValue() );
				idCount++;
			}
			// patient.name
			// Notice that patient.name is fullfilled by the method EN2HumanName.
			int nameCount = 0;
			for( EN pn : pr.getPatient().getNames() ){
				
				// patient.name.use
				if( pn.getUses() == null || pn.getUses().isEmpty() ){
					Assert.assertNull( patient.getName().get(nameCount).getUse() );
				} else{
					Assert.assertEquals("pr.patient.name["+nameCount+"]"+".use was not transformed", vsti.EntityNameUse2NameUseEnum(pn.getUses().get(0)).toString().toLowerCase(), patient.getName().get(nameCount).getUse() );
				}
				
				// patient.name.text
				Assert.assertEquals("pr.patient.name["+nameCount+"].text was not transformed", pn.getText(),patient.getName().get(nameCount).getText() );
				
				// patient.name.family
				int familyCount = 0;
				for( ENXP family : pn.getFamilies() ){
					if( family == null || family.isSetNullFlavor() ){
						// It can return null or an empty list
						Assert.assertTrue( patient.getName().get(nameCount).getFamily() == null || patient.getName().get(nameCount).getFamily().size() == 0  );
					} else{
						Assert.assertEquals("pr.patient.name["+nameCount+"].family was not transformed", family.getText(),patient.getName().get(nameCount).getFamily().get(familyCount).getValue());
					}
					familyCount++;
				}
				
				// patient.name.given
				int givenCount = 0;
				for( ENXP given : pn.getGivens() ){
					if( given == null || given.isSetNullFlavor() ){
						// It can return null or an empty list
						Assert.assertTrue( patient.getName().get(nameCount).getGiven() == null || patient.getName().get(nameCount).getGiven().size() == 0  );
					} else{
						Assert.assertEquals("pr.patient.name["+nameCount+"].given was not transformed", given.getText(),patient.getName().get(nameCount).getGiven().get(givenCount).getValue());
					}
					givenCount++;
				}
				
				// patient.name.prefix
				int prefixCount = 0;
				for( ENXP prefix : pn.getPrefixes() ){
					if( prefix == null || prefix.isSetNullFlavor() ){
						// It can return null or an empty list
						Assert.assertTrue( patient.getName().get(nameCount).getPrefix() == null || patient.getName().get(nameCount).getPrefix().size() == 0  );
					} else{
						Assert.assertEquals("pr.patient.name["+nameCount+"].prefix was not transformed", prefix.getText(),patient.getName().get(nameCount).getPrefix().get(prefixCount).getValue());
					}
					prefixCount++;
				}
				
				// patient.name.suffix
				int suffixCount = 0;
				for( ENXP suffix : pn.getPrefixes() ){
					if( suffix == null || suffix.isSetNullFlavor() ){
						// It can return null or an empty list
						Assert.assertTrue( patient.getName().get(nameCount).getSuffix() == null || patient.getName().get(nameCount).getSuffix().size() == 0  );
					} else{
						Assert.assertEquals("pr.patient.name["+nameCount+"].suffix was not transformed", suffix.getText(),patient.getName().get(nameCount).getSuffix().get(suffixCount).getValue());
					}
					suffixCount++;
				}
				
				// patient.name.period
				if( pn.getValidTime() == null || pn.getValidTime().isSetNullFlavor() ){
					// It can return null or an empty list
					Assert.assertTrue( patient.getName().get(nameCount).getPeriod() == null || patient.getName().get(nameCount).getPeriod().isEmpty() );
				} else{ // start of non-null period test
					
					if( pn.getValidTime().getLow() == null || pn.getValidTime().getLow().isSetNullFlavor() ){
						
						Assert.assertTrue( patient.getName().get(nameCount).getPeriod().getStart() == null );
					} else{
						System.out.println("Following lines should contain identical non-null dates:");
						System.out.println("[FHIR] " + patient.getName().get(nameCount).getPeriod().getStart());
						System.out.println("[CDA] "+ pn.getValidTime().getLow());
					}
					
					if( pn.getValidTime().getHigh() == null || pn.getValidTime().getHigh().isSetNullFlavor() ){
						Assert.assertTrue( patient.getName().get(nameCount).getPeriod().getEnd() == null );
					} else{
						System.out.println("Following lines should contain identical non-null dates:");
						System.out.println("[FHIR] " + patient.getName().get(nameCount).getPeriod().getEnd());
						System.out.println("[CDA] "+ pn.getValidTime().getHigh());
					}
				} // end of non-null period test
				nameCount++;
			} // end of patient.name tests
		
			
			// patient.telecom
			// Notice that patient.telecom is fullfilled by the method dtt.TEL2ContactPoint
			if( pr.getTelecoms() == null || pr.getTelecoms().isEmpty() ){
				Assert.assertTrue( patient.getTelecom() == null || patient.getTelecom().isEmpty() );
			} else{
				// size check
				Assert.assertTrue( pr.getTelecoms().size() == patient.getTelecom().size() );
				// We have already tested the method TEL2ContactPoint. Therefore, null-check and size-check is enough for now.
			}
			
			// patient.gender
			// vst.AdministrativeGenderCode2AdministrativeGenderEnum is used in this transformation.
			// Following test aims to test that ValueSetTransformer method.
			if( pr.getPatient().getAdministrativeGenderCode() == null || pr.getPatient().getAdministrativeGenderCode().isSetNullFlavor() ){
				Assert.assertTrue( patient.getGender() == null || patient.getGender().isEmpty() );
			} else{
				System.out.println( "Following lines should contain two lines of non-null gender information which are relevant(male,female,unknown):" );
				System.out.println("  [FHIR]: " + patient.getGender() );
				System.out.println( "  [CDA]: " + pr.getPatient().getAdministrativeGenderCode().getCode() );
			}
			
			// patient.birthDate
			// Notice that patient.birthDate is fullfilled by the method dtt.TS2Date 
			if( pr.getPatient().getBirthTime() == null || pr.getPatient().getBirthTime().isSetNullFlavor() ){
				Assert.assertTrue( patient.getBirthDate() == null );
			} else{
				System.out.println( "Following lines should contain two lines of non-null, equivalent birthdate information:" );
				System.out.println("  [FHIR]: " + patient.getBirthDate() );
				System.out.println("  [CDA]: "+ pr.getPatient().getBirthTime().getValue());
			}
			
			// patient.address
			// Notice that patient.address is fullfilled by the method dtt.AD2Address  
			if( pr.getAddrs() == null || pr.getAddrs().isEmpty() ){
				Assert.assertTrue( patient.getAddress() == null || patient.getAddress().isEmpty() );
			} else{
				// We have already tested the method AD2Address. Therefore, null-check and size-check is enough for now.
				Assert.assertTrue( pr.getAddrs().size() == patient.getAddress().size() );
			}
			
			// patient.maritalStatus
			// vst.MaritalStatusCode2MaritalStatusCodesEnum is used in this transformation.
			// Following test aims to test that ValueSetTransformer method.
			if( pr.getPatient().getMaritalStatusCode() == null || pr.getPatient().getMaritalStatusCode().isSetNullFlavor() ){
				Assert.assertTrue( patient.getMaritalStatus() == null || patient.getMaritalStatus().isEmpty() );
			} else{
				System.out.println( "Following lines should contain two lines of non-null, equivalent marital status information:" );
				System.out.println("  [FHIR]: " + patient.getMaritalStatus().getCoding().get(0).getCode() );
				System.out.println("  [CDA]: "+ pr.getPatient().getMaritalStatusCode().getCode() );
				
			}
			
			
			// patient.languageCommunication
			if( pr.getPatient().getLanguageCommunications() == null || pr.getPatient().getLanguageCommunications().isEmpty() ){
				Assert.assertTrue( patient.getCommunication() == null || patient.getCommunication().isEmpty() );
			} else{
				Assert.assertTrue( pr.getPatient().getLanguageCommunications().size() == patient.getCommunication().size() );
				
				int sizeCommunication = pr.getPatient().getLanguageCommunications().size() ;
				while( sizeCommunication != 0){
					
					//language
					if( pr.getPatient().getLanguageCommunications().get(sizeCommunication - 1).getLanguageCode() == null ||  
							pr.getPatient().getLanguageCommunications().get(0).getLanguageCode().isSetNullFlavor() ){
						Assert.assertTrue( patient.getCommunication().get(sizeCommunication - 1).getLanguage() == null || patient.getCommunication().get(sizeCommunication -1 ).getLanguage().isEmpty() );
					} else{
						// We have already tested the method CD2CodeableConcept. Therefore, null-check is enough for now.
					}
					
					// preference
					if( pr.getPatient().getLanguageCommunications().get(sizeCommunication - 1).getPreferenceInd() == null ||
							pr.getPatient().getLanguageCommunications().get(sizeCommunication - 1).getPreferenceInd().isSetNullFlavor() ){
						Assert.assertTrue( patient.getCommunication().get(sizeCommunication - 1).getPreferred() == null );
					} else{
						Assert.assertEquals(pr.getPatient().getLanguageCommunications().get(sizeCommunication - 1).getPreferenceInd().getValue(), patient.getCommunication().get(sizeCommunication - 1).getPreferred());
					}
					sizeCommunication--;
				}
				
			}
			
			// providerOrganization
			if( pr.getProviderOrganization() == null || pr.getProviderOrganization().isSetNullFlavor() ){
				Assert.assertTrue( patient.getManagingOrganization() == null || patient.getManagingOrganization().isEmpty() );
			} else{
				if( pr.getProviderOrganization().getNames() == null ){
					Assert.assertTrue( patient.getManagingOrganization().getDisplay() == null );
				}
				System.out.println("[FHIR] Reference for managing organization: "+  patient.getManagingOrganization().getReference() );
				
			}
			
			// guardian
			if( pr.getPatient().getGuardians() == null || pr.getPatient().getGuardians().isEmpty() ){
				Assert.assertTrue( patient.getContact() == null || patient.getContact().isEmpty() );
			} else{
				// Notice that, inside this mapping, the methods dtt.TEL2ContactPoint and dtt.AD2Address are used.
				// Therefore, null-check and size-check are enough
				Assert.assertTrue( pr.getPatient().getGuardians().size() == patient.getContact().size() );
			}
			
			// extensions
			int extCount = 0;
			for( ExtensionDt extension : patient.getUndeclaredExtensions() ){
				Assert.assertTrue( extension.getUrl() != null );
				Assert.assertTrue( extension.getValue() != null );
				System.out.println("[FHIR] Extension["+extCount+"] url: "+extension.getUrl());
				System.out.println("[FHIR] Extension["+ extCount++ +"] value: "+extension.getValue());
			}

			
			
			
		}
    }
}

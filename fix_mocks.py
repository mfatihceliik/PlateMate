import re

path = r'c:\Users\mfatihceliik\Desktop\platemate\src\test\java\com\mefy\platemate\business\concrete\PlateManagerTest.java'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('when(plateValidator.isValid("34ABC123")).thenReturn(true);', 'when(plateSearchService.normalizePlate(any())).thenAnswer(i -> i.getArgument(0).toString().replace(" ", ""));\n        when(plateSearchService.checkIfPlateValid("34ABC123")).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());')

content = content.replace('when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(plate));', 'when(plateSearchService.getOrCreatePlate("34ABC123")).thenReturn(plate);\n        when(plateSearchService.checkIfPlatePubliclyVisible(plate)).thenReturn(new com.mefy.platemate.core.utilities.results.SuccessResult());')

content = content.replace('when(plateDao.findByPlateCode("34ABC123")).thenReturn(Optional.of(hiddenPlate));', 'when(plateSearchService.getOrCreatePlate("34ABC123")).thenReturn(hiddenPlate);\n        when(plateSearchService.checkIfPlatePubliclyVisible(hiddenPlate)).thenReturn(new com.mefy.platemate.core.utilities.results.ErrorResult("plate-not-available"));')

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

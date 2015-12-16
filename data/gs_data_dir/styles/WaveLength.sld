<?xml version="1.0" encoding="UTF-8"?>
<StyledLayerDescriptor xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd" version="1.0.0">
   <NamedLayer>
      <Name>Wave Period</Name>
      <UserStyle>
         <Title>Wave period in seconds</Title>
         <Abstract>Wave period in seconds (max 60 sec)</Abstract>
         <FeatureTypeStyle>
            <Rule>
               <RasterSymbolizer>
                  <Opacity>1.0</Opacity>
                  <ColorMap type="ramp">
                     <ColorMapEntry color="#000000" quantity="0" opacity="0" />
                     <ColorMapEntry color="#FF0000" quantity="0.00001" opacity="1" label="0 m" />
                     <ColorMapEntry color="#FFFF00" quantity="3" opacity="1" label="3 m" />
                     <ColorMapEntry color="#00FFFF" quantity="15" opacity="1" label="15 m" />
                     <ColorMapEntry color="#0000FF" quantity="60" opacity="1" label="60 m" />
                     <ColorMapEntry color="#0000FF" quantity="100.00001" opacity="0" />
                     </ColorMap>
               </RasterSymbolizer>
            </Rule>
         </FeatureTypeStyle>
      </UserStyle>
   </NamedLayer>
</StyledLayerDescriptor>
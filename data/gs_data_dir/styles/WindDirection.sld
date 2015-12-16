<?xml version="1.0" encoding="UTF-8"?>
<StyledLayerDescriptor xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd" version="1.0.0">
   <NamedLayer>
      <Name>Wind speed</Name>
      <UserStyle>
         <Title>Wind direction deg</Title>
         <Abstract>Wind direction deg</Abstract>
         <FeatureTypeStyle>
            <Rule>
               <RasterSymbolizer>
                  <Opacity>1.0</Opacity>
                  <ColorMap type="ramp">
                    <ColorMapEntry color="#000000" quantity="-0" opacity="0" />
                    <ColorMapEntry color="#0000FF" quantity="0.00001" opacity="1" label="0 deg" />
                    <ColorMapEntry color="#000FFF" quantity="30" opacity="1" label="30 deg" />
                    <ColorMapEntry color="#00FFFF" quantity="90" opacity="1" label="90 deg" />
                    <ColorMapEntry color="#0FFFFF" quantity="120" opacity="1" label="120 deg" />
                    <ColorMapEntry color="#F00000" quantity="180" opacity="1" label="180 deg" />
                    <ColorMapEntry color="#FF0000" quantity="200" opacity="1" label="200 deg" />
                    <ColorMapEntry color="#FFF000" quantity="250" opacity="1" label="250 deg" />
                    <ColorMapEntry color="#FFFF00" quantity="300" opacity="1" label="300 deg" />
                    <ColorMapEntry color="#FFFFF0" quantity="320" opacity="1" label="320 deg" />
                    <ColorMapEntry color="#FF00FF" quantity="345" opacity="1" label="345 deg" />
                    <ColorMapEntry color="#F0000F" quantity="360" opacity="1" label="360 deg" />
                    <ColorMapEntry color="#FFFFFF" quantity="360.00001" opacity="0" />
                  </ColorMap>
               </RasterSymbolizer>
            </Rule>
         </FeatureTypeStyle>
      </UserStyle>
   </NamedLayer>
</StyledLayerDescriptor>
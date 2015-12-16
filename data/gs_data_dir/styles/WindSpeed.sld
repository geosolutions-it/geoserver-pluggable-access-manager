<?xml version="1.0" encoding="UTF-8"?>
<StyledLayerDescriptor xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd" version="1.0.0">
   <NamedLayer>
      <Name>Wind speed</Name>
      <UserStyle>
         <Title>Wind speed m/s</Title>
         <Abstract>Wind speed m/s (max 55 m/s=200 km/h)</Abstract>
         <FeatureTypeStyle>
            <Rule>
               <RasterSymbolizer>
                  <Opacity>1.0</Opacity>
                  <ColorMap type="ramp">
                     <ColorMapEntry color="#000000" quantity="-0" opacity="0" />
                    <ColorMapEntry color="#0000FF" quantity="0.00001" opacity="1" label="0 m/s" />
                    <ColorMapEntry color="#00FFFF" quantity="2" opacity="1" label="2 m/s" />
                    <ColorMapEntry color="#FFFF00" quantity="3" opacity="1" label="3 m/s" />
                    <ColorMapEntry color="#FF0000" quantity="4" opacity="1" label="4 m/s" />
                    <ColorMapEntry color="#FF0000" quantity="5" opacity="1" label="5 m/s" />
                    <ColorMapEntry color="#FF0000" quantity="15" opacity="1" label="15 m/s" />
                    <ColorMapEntry color="#FF0000" quantity="20" opacity="1" label="20 m/s" />
                    <ColorMapEntry color="#FF0000" quantity="30" opacity="1" label="30 m/s" />
                    <ColorMapEntry color="#FF0000" quantity="40" opacity="1" label="40 m/s" />
                    <ColorMapEntry color="#FF0000" quantity="50" opacity="1" label="50 m/s" />
                    <ColorMapEntry color="#FF0000" quantity="55" opacity="1" label="55 m/s" />
                    <ColorMapEntry color="#FF0000" quantity="55.00001" opacity="0" />
                  </ColorMap>
               </RasterSymbolizer>
            </Rule>
         </FeatureTypeStyle>
      </UserStyle>
   </NamedLayer>
</StyledLayerDescriptor>
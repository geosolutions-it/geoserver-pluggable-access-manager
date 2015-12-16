<?xml version="1.0" encoding="ISO-8859-1"?>
  <StyledLayerDescriptor version="1.0.0"
   xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd"
   xmlns="http://www.opengis.net/sld"
   xmlns:ogc="http://www.opengis.net/ogc"
   xmlns:xlink="http://www.w3.org/1999/xlink"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <NamedLayer>
      <Name>ship_detections</Name>
      <UserStyle>
      <!-- Styles can have names, titles and abstracts -->
        <Title>Ships</Title>
        <Abstract>Styles ships using stacked points</Abstract>
        <!-- The style without any transformation, used when the point aren't clustered -->
        <FeatureTypeStyle>
        <Rule>
            <Name>rule1</Name>
            <Title> A single ship with getFeatureInfo avaiable.</Title>
            <PointSymbolizer>
              <Graphic>
                <Mark>
                  <WellKnownName>triangle</WellKnownName>
                  <Fill>
                    <CssParameter name="fill">#008000</CssParameter>
                  </Fill>
                  <Stroke>
                    <CssParameter name="stroke">#ffffff</CssParameter>
                  </Stroke>
                </Mark>
              <Size>12</Size>
            </Graphic>
          </PointSymbolizer>
         <!-- <TextSymbolizer>
            <Label>
              ID: <ogc:PropertyName>dsid</ogc:PropertyName>
            </Label>
            <Font>
              <CssParameter name="font-family">Arial</CssParameter>
              <CssParameter name="font-size">10</CssParameter>
              <CssParameter name="font-style">normal</CssParameter>
              <CssParameter name="font-weight">normal</CssParameter>
              <CssParameter name="font-color">red</CssParameter>
            </Font>
            <LabelPlacement>
                <PointPlacement>
                <AnchorPoint>
                  <AnchorPointX>-0.03</AnchorPointX>
                  <AnchorPointY>-0.03</AnchorPointY>
                </AnchorPoint>
                <Rotation>-45</Rotation>
              </PointPlacement>
            </LabelPlacement>
            <Halo>
              <Radius>1</Radius>
              <Fill>
                <CssParameter name="fill">#FFFFFF</CssParameter>
              </Fill>
            </Halo>
            <VendorOption name="conflictResolution">true</VendorOption>
            <VendorOption name="partials">true</VendorOption>
          </TextSymbolizer> -->
          </Rule>
      </FeatureTypeStyle>
        
      </UserStyle>
    </NamedLayer>
  </StyledLayerDescriptor>
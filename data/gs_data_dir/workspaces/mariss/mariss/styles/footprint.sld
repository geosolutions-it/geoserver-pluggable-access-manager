<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0" 
 xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd" 
 xmlns="http://www.opengis.net/sld"
 xmlns:ogc="http://www.opengis.net/ogc" 
 xmlns:xlink="http://www.w3.org/1999/xlink" 
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <!-- a Named Layer is the basic building block of an SLD document -->
  <NamedLayer>
    <Name>default_line</Name>
    <UserStyle>
    <!-- Styles can have names, titles and abstracts -->
      <Title>Default Line</Title>
      <Abstract>A sample style that draws a line</Abstract>
      <!-- FeatureTypeStyles describe how to render different features -->
      <!-- A FeatureTypeStyle for rendering lines -->
      <FeatureTypeStyle>
        <Rule>
          <Name>Footprint</Name>
          <Title>Footprint</Title>
          <Abstract>A solid red line with a 1 pixel width and a label</Abstract>
          <LineSymbolizer>
            <Stroke>
              <CssParameter name="stroke">#ff0000</CssParameter>
            </Stroke>
          </LineSymbolizer>
        </Rule>
      </FeatureTypeStyle>
      
      <FeatureTypeStyle>
        <Rule>
          <Name>Time</Name>
          <Title>The time of the granule represented by this footprint</Title>
          <Abstract>UTC time in ISO format</Abstract>
          <MaxScaleDenominator>2500000</MaxScaleDenominator>
          <TextSymbolizer>
            <Geometry>
              <ogc:Function name="startPoint">
                <ogc:PropertyName>the_geom</ogc:PropertyName>
              </ogc:Function>
            </Geometry>
            <Label>
              <ogc:PropertyName>time</ogc:PropertyName>
              <ogc:Literal>UTC</ogc:Literal>
            </Label>
            <Font>
              <CssParameter name="font-family">Arial</CssParameter>
              <CssParameter name="font-size">10</CssParameter>
              <CssParameter name="font-style">normal</CssParameter>
              <CssParameter name="font-weight">bold</CssParameter>
              <CssParameter name="font-color">black</CssParameter>
            </Font>
            <LabelPlacement>
                <PointPlacement>
                  <AnchorPoint>
                    <AnchorPointX>0</AnchorPointX>
                    <AnchorPointY>0</AnchorPointY>
                  </AnchorPoint>
                  <Displacement>
                    <DisplacementX>5</DisplacementX>
                    <DisplacementY>5</DisplacementY>
                  </Displacement>
              </PointPlacement>
            </LabelPlacement>
            <Halo>
              <Radius>
                <ogc:Literal>2</ogc:Literal>
              </Radius>
              <Fill>
                <CssParameter name="fill">#ffffff</CssParameter>
              </Fill>
            </Halo>
            <VendorOption name="spaceAround">-100</VendorOption>            
            <Fill>
                <CssParameter name="fill">#000000</CssParameter>
            </Fill>
            <!--
            <VendorOption name="group">yes</VendorOption>
            -->
          </TextSymbolizer>
        </Rule>
      </FeatureTypeStyle>
      
    </UserStyle>
  </NamedLayer>
</StyledLayerDescriptor>
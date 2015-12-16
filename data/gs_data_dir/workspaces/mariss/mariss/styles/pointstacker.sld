<?xml version="1.0" encoding="ISO-8859-1"?>
  <StyledLayerDescriptor version="1.0.0"
   xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd"
   xmlns="http://www.opengis.net/sld"
   xmlns:ogc="http://www.opengis.net/ogc"
   xmlns:xlink="http://www.w3.org/1999/xlink"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <NamedLayer>
      <Name>tem_sd__1p</Name>
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
          <TextSymbolizer>
            <Label>
              class <ogc:PropertyName>size_class</ogc:PropertyName>
            </Label>
            <Font>
              <CssParameter name="font-family">Arial</CssParameter>
              <CssParameter name="font-size">10</CssParameter>
              <CssParameter name="font-style">normal</CssParameter>
              <CssParameter name="font-weight">normal</CssParameter>
            </Font>
            <LabelPlacement>
                <PointPlacement>
                <AnchorPoint>
                  <AnchorPointX>-0.1</AnchorPointX>
                  <AnchorPointY>-0.1</AnchorPointY>
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
          </TextSymbolizer>
          </Rule>
      </FeatureTypeStyle>
        
        <!-- The style with transformations, used when the point are clustered -->        
        <FeatureTypeStyle>
          <Transformation>
            <ogc:Function name="gs:PointStacker">
              <ogc:Function name="parameter">
                <ogc:Literal>data</ogc:Literal>
              </ogc:Function>
              <ogc:Function name="parameter">
                <ogc:Literal>cellSize</ogc:Literal>
                <ogc:Literal>20</ogc:Literal>
              </ogc:Function>
              <ogc:Function name="parameter">
                <ogc:Literal>outputBBOX</ogc:Literal>
                <ogc:Function name="env">
               <ogc:Literal>wms_bbox</ogc:Literal>
                </ogc:Function>
              </ogc:Function>
              <ogc:Function name="parameter">
                <ogc:Literal>outputWidth</ogc:Literal>
                <ogc:Function name="env">
               <ogc:Literal>wms_width</ogc:Literal>
                </ogc:Function>
              </ogc:Function>
              <ogc:Function name="parameter">
                <ogc:Literal>outputHeight</ogc:Literal>
                <ogc:Function name="env">
                  <ogc:Literal>wms_height</ogc:Literal>
                </ogc:Function>
              </ogc:Function>
            </ogc:Function>
          </Transformation>
          
          
          <Rule>
            <MinScaleDenominator>2500000</MinScaleDenominator>
            <Name>rule29</Name>
            <Title> A group of 2-9 ships</Title>
            <ogc:Filter>
              <ogc:PropertyIsBetween>
                <ogc:PropertyName>count</ogc:PropertyName>
                <ogc:LowerBoundary>
                  <ogc:Literal>2</ogc:Literal>
                </ogc:LowerBoundary>
                <ogc:UpperBoundary>
                  <ogc:Literal>9</ogc:Literal>
                </ogc:UpperBoundary>
              </ogc:PropertyIsBetween>
            </ogc:Filter>
            <PointSymbolizer>
              <Graphic>
                <Mark>
                  <WellKnownName>circle</WellKnownName>
                  <Fill>
                    <CssParameter name="fill">#008000</CssParameter>
                  </Fill>
                </Mark>
                <Size>19</Size>
              </Graphic>
            </PointSymbolizer>
            <TextSymbolizer>
              <Label>
                <ogc:PropertyName>count</ogc:PropertyName>
              </Label>
              <Font>
                <CssParameter name="font-family">Arial</CssParameter>
                <CssParameter name="font-size">12</CssParameter>
                <CssParameter name="font-weight">bold</CssParameter>
              </Font>
              <LabelPlacement>
                <PointPlacement>
                <AnchorPoint>
                  <AnchorPointX>0.5</AnchorPointX>
                  <AnchorPointY>0.8</AnchorPointY>
                </AnchorPoint>
                </PointPlacement>
              </LabelPlacement>
              <Fill>
                <CssParameter name="fill">#FFFFFF</CssParameter>
                <CssParameter name="fill-opacity">1.0</CssParameter>
              </Fill>
            </TextSymbolizer>
          </Rule>
          <Rule>
            <Name>rule10</Name>
            <Title> A group of 10 ships or more</Title>
            <ogc:Filter>
              <ogc:PropertyIsGreaterThan>
                <ogc:PropertyName>count</ogc:PropertyName>
                <ogc:Literal>9</ogc:Literal>
              </ogc:PropertyIsGreaterThan>
            </ogc:Filter>
            <PointSymbolizer>
              <Graphic>
                <Mark>
                  <WellKnownName>circle</WellKnownName>
                  <Fill>
                    <CssParameter name="fill">#008000</CssParameter>
                  </Fill>
                </Mark>
                <Size>25</Size>
              </Graphic>
            </PointSymbolizer>
            <TextSymbolizer>
              <Label>
                <ogc:PropertyName>count</ogc:PropertyName>
              </Label>
              <Font>
                <CssParameter name="font-family">Arial</CssParameter>
                <CssParameter name="font-size">12</CssParameter>
                <CssParameter name="font-weight">bold</CssParameter>
              </Font>
              <LabelPlacement>
                <PointPlacement>
                  <AnchorPoint>
                    <AnchorPointX>0.5</AnchorPointX>
                    <AnchorPointY>0.8</AnchorPointY>
                  </AnchorPoint>
                </PointPlacement>
              </LabelPlacement>
              <Fill>
                <CssParameter name="fill">#FFFFFF</CssParameter>
                <CssParameter name="fill-opacity">1.0</CssParameter>
              </Fill>
            </TextSymbolizer>
          </Rule>
        </FeatureTypeStyle>
      </UserStyle>
    </NamedLayer>
  </StyledLayerDescriptor>
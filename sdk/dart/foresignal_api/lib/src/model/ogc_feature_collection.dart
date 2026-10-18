//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ogc_link.dart';
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/ogc_feature.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_feature_collection.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcFeatureCollection {
  /// Returns a new [OgcFeatureCollection] instance.
  OgcFeatureCollection({

    required  this.type,

    required  this.features,

    required  this.links,

    required  this.numberReturned,

     this.numberMatched,

    required  this.timeStamp,

     this.attributions,

     this.licenceFiltered,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final OgcFeatureCollectionTypeEnum type;



  @JsonKey(
    
    name: r'features',
    required: true,
    includeIfNull: false,
  )


  final List<OgcFeature> features;



      /// self, next (opaque keyset cursor), collection
  @JsonKey(
    
    name: r'links',
    required: true,
    includeIfNull: false,
  )


  final List<OgcLink> links;



  @JsonKey(
    
    name: r'numberReturned',
    required: true,
    includeIfNull: false,
  )


  final int numberReturned;



      /// Only when count=true and at most 100,000
  @JsonKey(
    
    name: r'numberMatched',
    required: false,
    includeIfNull: false,
  )


  final int? numberMatched;



  @JsonKey(
    
    name: r'timeStamp',
    required: true,
    includeIfNull: false,
  )


  final DateTime timeStamp;



  @JsonKey(
    
    name: r'attributions',
    required: false,
    includeIfNull: false,
  )


  final List<Attribution>? attributions;



          // minimum: 0
  @JsonKey(
    
    name: r'licenceFiltered',
    required: false,
    includeIfNull: false,
  )


  final int? licenceFiltered;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcFeatureCollection &&
      other.type == type &&
      other.features == features &&
      other.links == links &&
      other.numberReturned == numberReturned &&
      other.numberMatched == numberMatched &&
      other.timeStamp == timeStamp &&
      other.attributions == attributions &&
      other.licenceFiltered == licenceFiltered;

    @override
    int get hashCode =>
        type.hashCode +
        features.hashCode +
        links.hashCode +
        numberReturned.hashCode +
        numberMatched.hashCode +
        timeStamp.hashCode +
        attributions.hashCode +
        licenceFiltered.hashCode;

  factory OgcFeatureCollection.fromJson(Map<String, dynamic> json) => _$OgcFeatureCollectionFromJson(json);

  Map<String, dynamic> toJson() => _$OgcFeatureCollectionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum OgcFeatureCollectionTypeEnum {
@JsonValue(r'FeatureCollection')
featureCollection(r'FeatureCollection');

const OgcFeatureCollectionTypeEnum(this.value);

final String value;

@override
String toString() => value;
}



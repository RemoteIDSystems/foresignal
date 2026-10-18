//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'collection_gap_recommendations_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CollectionGapRecommendationsInner {
  /// Returns a new [CollectionGapRecommendationsInner] instance.
  CollectionGapRecommendationsInner({

    required  this.type,

     this.sourceId,

     this.sensorFamily,

    required  this.text,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final CollectionGapRecommendationsInnerTypeEnum type;



  @JsonKey(
    
    name: r'sourceId',
    required: false,
    includeIfNull: false,
  )


  final String? sourceId;



      /// e.g. 069, 078 or 086 receiver or sensor family
  @JsonKey(
    
    name: r'sensorFamily',
    required: false,
    includeIfNull: false,
  )


  final String? sensorFamily;



  @JsonKey(
    
    name: r'text',
    required: true,
    includeIfNull: false,
  )


  final String text;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CollectionGapRecommendationsInner &&
      other.type == type &&
      other.sourceId == sourceId &&
      other.sensorFamily == sensorFamily &&
      other.text == text;

    @override
    int get hashCode =>
        type.hashCode +
        (sourceId == null ? 0 : sourceId.hashCode) +
        (sensorFamily == null ? 0 : sensorFamily.hashCode) +
        text.hashCode;

  factory CollectionGapRecommendationsInner.fromJson(Map<String, dynamic> json) => _$CollectionGapRecommendationsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$CollectionGapRecommendationsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum CollectionGapRecommendationsInnerTypeEnum {
@JsonValue(r'enable-source')
enableSource(r'enable-source'),
@JsonValue(r'add-receiver')
addReceiver(r'add-receiver'),
@JsonValue(r'add-sensor')
addSensor(r'add-sensor'),
@JsonValue(r'none')
none(r'none');

const CollectionGapRecommendationsInnerTypeEnum(this.value);

final String value;

@override
String toString() => value;
}



//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/quantiles.dart';
import 'package:foresignal_api/src/model/edp_forecast_item_all_of_question.dart';
import 'package:foresignal_api/src/model/edp_origin.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_forecast_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpForecastItem {
  /// Returns a new [EdpForecastItem] instance.
  EdpForecastItem({

    required  this.stream,

    required  this.type,

    required  this.id,

     this.observedAt,

     this.properties,

     this.question,

     this.questionRef,

     this.probability,

     this.quantiles,

    required  this.madeAt,

     this.origin,
  });

  @JsonKey(
    
    name: r'stream',
    required: true,
    includeIfNull: false,
  )


  final String stream;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final EdpForecastItemTypeEnum type;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'observedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? observedAt;



      /// Item properties, validated against the stream's payloadSchema
  @JsonKey(
    
    name: r'properties',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? properties;



  @JsonKey(
    
    name: r'question',
    required: false,
    includeIfNull: false,
  )


  final EdpForecastItemAllOfQuestion? question;



  @JsonKey(
    
    name: r'questionRef',
    required: false,
    includeIfNull: false,
  )


  final String? questionRef;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'probability',
    required: false,
    includeIfNull: false,
  )


  final num? probability;



  @JsonKey(
    
    name: r'quantiles',
    required: false,
    includeIfNull: false,
  )


  final Quantiles? quantiles;



      /// Provider-claimed time; the recorded made_at is max(madeAt, receipt time)
  @JsonKey(
    
    name: r'madeAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime madeAt;



  @JsonKey(
    
    name: r'origin',
    required: false,
    includeIfNull: false,
  )


  final EdpOrigin? origin;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpForecastItem &&
      other.stream == stream &&
      other.type == type &&
      other.id == id &&
      other.observedAt == observedAt &&
      other.properties == properties &&
      other.question == question &&
      other.questionRef == questionRef &&
      other.probability == probability &&
      other.quantiles == quantiles &&
      other.madeAt == madeAt &&
      other.origin == origin;

    @override
    int get hashCode =>
        stream.hashCode +
        type.hashCode +
        id.hashCode +
        observedAt.hashCode +
        properties.hashCode +
        (question == null ? 0 : question.hashCode) +
        (questionRef == null ? 0 : questionRef.hashCode) +
        (probability == null ? 0 : probability.hashCode) +
        (quantiles == null ? 0 : quantiles.hashCode) +
        madeAt.hashCode +
        (origin == null ? 0 : origin.hashCode);

  factory EdpForecastItem.fromJson(Map<String, dynamic> json) => _$EdpForecastItemFromJson(json);

  Map<String, dynamic> toJson() => _$EdpForecastItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EdpForecastItemTypeEnum {
@JsonValue(r'forecast')
forecast(r'forecast');

const EdpForecastItemTypeEnum(this.value);

final String value;

@override
String toString() => value;
}



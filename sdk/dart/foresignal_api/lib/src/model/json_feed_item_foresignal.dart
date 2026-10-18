//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'json_feed_item_foresignal.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class JsonFeedItemForesignal {
  /// Returns a new [JsonFeedItemForesignal] instance.
  JsonFeedItemForesignal({

     this.severity,

     this.state,

     this.sourceId,

     this.externalId,

     this.geometry,
  });

  @JsonKey(
    
    name: r'severity',
    required: false,
    includeIfNull: false,
  )


  final JsonFeedItemForesignalSeverityEnum? severity;



  @JsonKey(
    
    name: r'state',
    required: false,
    includeIfNull: false,
  )


  final JsonFeedItemForesignalStateEnum? state;



  @JsonKey(
    
    name: r'sourceId',
    required: false,
    includeIfNull: false,
  )


  final String? sourceId;



  @JsonKey(
    
    name: r'externalId',
    required: false,
    includeIfNull: false,
  )


  final String? externalId;



  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;





    @override
    bool operator ==(Object other) => identical(this, other) || other is JsonFeedItemForesignal &&
      other.severity == severity &&
      other.state == state &&
      other.sourceId == sourceId &&
      other.externalId == externalId &&
      other.geometry == geometry;

    @override
    int get hashCode =>
        severity.hashCode +
        state.hashCode +
        sourceId.hashCode +
        externalId.hashCode +
        (geometry == null ? 0 : geometry.hashCode);

  factory JsonFeedItemForesignal.fromJson(Map<String, dynamic> json) => _$JsonFeedItemForesignalFromJson(json);

  Map<String, dynamic> toJson() => _$JsonFeedItemForesignalToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum JsonFeedItemForesignalSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const JsonFeedItemForesignalSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


enum JsonFeedItemForesignalStateEnum {
@JsonValue(r'firing')
firing(r'firing'),
@JsonValue(r'acknowledged')
acknowledged(r'acknowledged'),
@JsonValue(r'resolved')
resolved(r'resolved');

const JsonFeedItemForesignalStateEnum(this.value);

final String value;

@override
String toString() => value;
}



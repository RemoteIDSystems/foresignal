//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'taf_payload_forecast_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TafPayloadForecastInner {
  /// Returns a new [TafPayloadForecastInner] instance.
  TafPayloadForecastInner({

     this.changeIndicator,

     this.from,

     this.to,

     this.wind,

     this.visibility,

     this.wx,

     this.clouds,
  });

      /// FM, TEMPO, BECMG, PROB30/40
  @JsonKey(
    
    name: r'changeIndicator',
    required: false,
    includeIfNull: false,
  )


  final String? changeIndicator;



  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final DateTime? from;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  )


  final DateTime? to;



  @JsonKey(
    
    name: r'wind',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? wind;



  @JsonKey(
    
    name: r'visibility',
    required: false,
    includeIfNull: false,
  )


  final String? visibility;



  @JsonKey(
    
    name: r'wx',
    required: false,
    includeIfNull: false,
  )


  final List<String>? wx;



  @JsonKey(
    
    name: r'clouds',
    required: false,
    includeIfNull: false,
  )


  final List<Map<String, Object>>? clouds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TafPayloadForecastInner &&
      other.changeIndicator == changeIndicator &&
      other.from == from &&
      other.to == to &&
      other.wind == wind &&
      other.visibility == visibility &&
      other.wx == wx &&
      other.clouds == clouds;

    @override
    int get hashCode =>
        (changeIndicator == null ? 0 : changeIndicator.hashCode) +
        (from == null ? 0 : from.hashCode) +
        (to == null ? 0 : to.hashCode) +
        (wind == null ? 0 : wind.hashCode) +
        (visibility == null ? 0 : visibility.hashCode) +
        wx.hashCode +
        clouds.hashCode;

  factory TafPayloadForecastInner.fromJson(Map<String, dynamic> json) => _$TafPayloadForecastInnerFromJson(json);

  Map<String, dynamic> toJson() => _$TafPayloadForecastInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


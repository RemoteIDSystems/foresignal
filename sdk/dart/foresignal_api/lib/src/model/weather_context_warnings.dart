//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_section_status.dart';
import 'package:foresignal_api/src/model/weather_context_warnings_items_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'weather_context_warnings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WeatherContextWarnings {
  /// Returns a new [WeatherContextWarnings] instance.
  WeatherContextWarnings({

    required  this.status,

     this.asOf,

     this.statusText,

     this.items,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final WeatherSectionStatus status;



      /// Time of the last good data; present only while status is not ok, fixed while not ok
  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;



      /// Explanation for non-ok statuses
  @JsonKey(
    
    name: r'statusText',
    required: false,
    includeIfNull: false,
  )


  final String? statusText;



  @JsonKey(
    
    name: r'items',
    required: false,
    includeIfNull: false,
  )


  final List<WeatherContextWarningsItemsInner>? items;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WeatherContextWarnings &&
      other.status == status &&
      other.asOf == asOf &&
      other.statusText == statusText &&
      other.items == items;

    @override
    int get hashCode =>
        status.hashCode +
        asOf.hashCode +
        statusText.hashCode +
        items.hashCode;

  factory WeatherContextWarnings.fromJson(Map<String, dynamic> json) => _$WeatherContextWarningsFromJson(json);

  Map<String, dynamic> toJson() => _$WeatherContextWarningsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


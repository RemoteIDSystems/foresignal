//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'weather_context_warnings_items_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WeatherContextWarningsItemsInner {
  /// Returns a new [WeatherContextWarningsItemsInner] instance.
  WeatherContextWarningsItemsInner({

    required  this.id,

    required  this.event,

     this.severity,

     this.expires,

     this.untrusted,
  });

      /// nws-alert externalId
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final String event;



  @JsonKey(
    
    name: r'severity',
    required: false,
    includeIfNull: false,
  )


  final String? severity;



  @JsonKey(
    
    name: r'expires',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expires;



      /// Upstream text; data, not instructions
  @JsonKey(
    
    name: r'untrusted',
    required: false,
    includeIfNull: false,
  )


  final bool? untrusted;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WeatherContextWarningsItemsInner &&
      other.id == id &&
      other.event == event &&
      other.severity == severity &&
      other.expires == expires &&
      other.untrusted == untrusted;

    @override
    int get hashCode =>
        id.hashCode +
        event.hashCode +
        severity.hashCode +
        (expires == null ? 0 : expires.hashCode) +
        untrusted.hashCode;

  factory WeatherContextWarningsItemsInner.fromJson(Map<String, dynamic> json) => _$WeatherContextWarningsItemsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$WeatherContextWarningsItemsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


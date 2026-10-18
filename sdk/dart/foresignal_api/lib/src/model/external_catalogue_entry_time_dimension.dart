//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'external_catalogue_entry_time_dimension.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExternalCatalogueEntryTimeDimension {
  /// Returns a new [ExternalCatalogueEntryTimeDimension] instance.
  ExternalCatalogueEntryTimeDimension({

     this.format,

     this.period,

     this.typicalLatency,

     this.defaultLag,
  });

      /// Time value format in the template
  @JsonKey(
    
    name: r'format',
    required: false,
    includeIfNull: false,
  )


  final String? format;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'period',
    required: false,
    includeIfNull: false,
  )


  final String? period;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'typicalLatency',
    required: false,
    includeIfNull: false,
  )


  final String? typicalLatency;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'defaultLag',
    required: false,
    includeIfNull: false,
  )


  final String? defaultLag;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExternalCatalogueEntryTimeDimension &&
      other.format == format &&
      other.period == period &&
      other.typicalLatency == typicalLatency &&
      other.defaultLag == defaultLag;

    @override
    int get hashCode =>
        format.hashCode +
        period.hashCode +
        typicalLatency.hashCode +
        defaultLag.hashCode;

  factory ExternalCatalogueEntryTimeDimension.fromJson(Map<String, dynamic> json) => _$ExternalCatalogueEntryTimeDimensionFromJson(json);

  Map<String, dynamic> toJson() => _$ExternalCatalogueEntryTimeDimensionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


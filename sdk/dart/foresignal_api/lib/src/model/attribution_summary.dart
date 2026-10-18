//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'attribution_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AttributionSummary {
  /// Returns a new [AttributionSummary] instance.
  AttributionSummary({

    required  this.attributions,

    required  this.text,
  });

      /// One entry per source and licence with the number of matching current records
  @JsonKey(
    
    name: r'attributions',
    required: true,
    includeIfNull: false,
  )


  final List<Attribution> attributions;



      /// Joined attributionText (at most 1 KB, truncated with …), the value of the Foresignal-Attribution header
  @JsonKey(
    
    name: r'text',
    required: true,
    includeIfNull: false,
  )


  final String text;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AttributionSummary &&
      other.attributions == attributions &&
      other.text == text;

    @override
    int get hashCode =>
        attributions.hashCode +
        text.hashCode;

  factory AttributionSummary.fromJson(Map<String, dynamic> json) => _$AttributionSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$AttributionSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


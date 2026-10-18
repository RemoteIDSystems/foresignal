//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_conformance.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcConformance {
  /// Returns a new [OgcConformance] instance.
  OgcConformance({

    required  this.conformsTo,
  });

  @JsonKey(
    
    name: r'conformsTo',
    required: true,
    includeIfNull: false,
  )


  final List<String> conformsTo;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcConformance &&
      other.conformsTo == conformsTo;

    @override
    int get hashCode =>
        conformsTo.hashCode;

  factory OgcConformance.fromJson(Map<String, dynamic> json) => _$OgcConformanceFromJson(json);

  Map<String, dynamic> toJson() => _$OgcConformanceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


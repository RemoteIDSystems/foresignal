//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/declared_observability_kinds_value.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'declared_observability.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DeclaredObservability {
  /// Returns a new [DeclaredObservability] instance.
  DeclaredObservability({

    required  this.kinds,
  });

  @JsonKey(
    
    name: r'kinds',
    required: true,
    includeIfNull: false,
  )


  final Map<String, DeclaredObservabilityKindsValue> kinds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DeclaredObservability &&
      other.kinds == kinds;

    @override
    int get hashCode =>
        kinds.hashCode;

  factory DeclaredObservability.fromJson(Map<String, dynamic> json) => _$DeclaredObservabilityFromJson(json);

  Map<String, dynamic> toJson() => _$DeclaredObservabilityToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


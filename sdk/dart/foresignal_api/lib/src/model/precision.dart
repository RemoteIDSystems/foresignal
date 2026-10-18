//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/precision_components_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precision.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Precision {
  /// Returns a new [Precision] instance.
  Precision({

     this.since,

    required  this.generatedAt,

    required  this.components,
  });

  @JsonKey(
    
    name: r'since',
    required: false,
    includeIfNull: false,
  )


  final DateTime? since;



  @JsonKey(
    
    name: r'generatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime generatedAt;



  @JsonKey(
    
    name: r'components',
    required: true,
    includeIfNull: false,
  )


  final List<PrecisionComponentsInner> components;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Precision &&
      other.since == since &&
      other.generatedAt == generatedAt &&
      other.components == components;

    @override
    int get hashCode =>
        (since == null ? 0 : since.hashCode) +
        generatedAt.hashCode +
        components.hashCode;

  factory Precision.fromJson(Map<String, dynamic> json) => _$PrecisionFromJson(json);

  Map<String, dynamic> toJson() => _$PrecisionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


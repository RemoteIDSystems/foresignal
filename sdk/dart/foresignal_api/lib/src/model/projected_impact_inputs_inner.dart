//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'projected_impact_inputs_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProjectedImpactInputsInner {
  /// Returns a new [ProjectedImpactInputsInner] instance.
  ProjectedImpactInputsInner({

    required  this.name,

     this.value,

     this.ref,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'value',
    required: false,
    includeIfNull: false,
  )


  final Object? value;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProjectedImpactInputsInner &&
      other.name == name &&
      other.value == value &&
      other.ref == ref;

    @override
    int get hashCode =>
        name.hashCode +
        (value == null ? 0 : value.hashCode) +
        (ref == null ? 0 : ref.hashCode);

  factory ProjectedImpactInputsInner.fromJson(Map<String, dynamic> json) => _$ProjectedImpactInputsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ProjectedImpactInputsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


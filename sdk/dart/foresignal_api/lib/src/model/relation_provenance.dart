//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'relation_provenance.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RelationProvenance {
  /// Returns a new [RelationProvenance] instance.
  RelationProvenance({

     this.component,

     this.version,

     this.manifestId,

     this.operator_,
  });

  @JsonKey(
    
    name: r'component',
    required: false,
    includeIfNull: false,
  )


  final String? component;



  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final String? version;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;



      /// Principal id
  @JsonKey(
    
    name: r'operator',
    required: false,
    includeIfNull: false,
  )


  final String? operator_;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RelationProvenance &&
      other.component == component &&
      other.version == version &&
      other.manifestId == manifestId &&
      other.operator_ == operator_;

    @override
    int get hashCode =>
        (component == null ? 0 : component.hashCode) +
        (version == null ? 0 : version.hashCode) +
        (manifestId == null ? 0 : manifestId.hashCode) +
        (operator_ == null ? 0 : operator_.hashCode);

  factory RelationProvenance.fromJson(Map<String, dynamic> json) => _$RelationProvenanceFromJson(json);

  Map<String, dynamic> toJson() => _$RelationProvenanceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


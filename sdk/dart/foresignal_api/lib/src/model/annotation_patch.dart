//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'annotation_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnnotationPatch {
  /// Returns a new [AnnotationPatch] instance.
  AnnotationPatch({

     this.verdict,

     this.duplicateOfRef,

     this.body,

     this.tags,

     this.private,
  });

  @JsonKey(
    
    name: r'verdict',
    required: false,
    includeIfNull: false,
  )


  final AnnotationPatchVerdictEnum? verdict;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'duplicateOfRef',
    required: false,
    includeIfNull: false,
  )


  final String? duplicateOfRef;



  @JsonKey(
    
    name: r'body',
    required: false,
    includeIfNull: false,
  )


  final String? body;



  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;



  @JsonKey(
    
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnnotationPatch &&
      other.verdict == verdict &&
      other.duplicateOfRef == duplicateOfRef &&
      other.body == body &&
      other.tags == tags &&
      other.private == private;

    @override
    int get hashCode =>
        verdict.hashCode +
        (duplicateOfRef == null ? 0 : duplicateOfRef.hashCode) +
        (body == null ? 0 : body.hashCode) +
        tags.hashCode +
        private.hashCode;

  factory AnnotationPatch.fromJson(Map<String, dynamic> json) => _$AnnotationPatchFromJson(json);

  Map<String, dynamic> toJson() => _$AnnotationPatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnnotationPatchVerdictEnum {
@JsonValue(r'confirmed')
confirmed(r'confirmed'),
@JsonValue(r'rejected')
rejected(r'rejected'),
@JsonValue(r'needs-review')
needsReview(r'needs-review'),
@JsonValue(r'duplicate-of')
duplicateOf(r'duplicate-of');

const AnnotationPatchVerdictEnum(this.value);

final String value;

@override
String toString() => value;
}



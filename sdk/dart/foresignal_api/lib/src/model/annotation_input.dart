//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'annotation_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnnotationInput {
  /// Returns a new [AnnotationInput] instance.
  AnnotationInput({

    required  this.kind,

     this.verdict,

     this.duplicateOfRef,

     this.body,

     this.tags,

     this.private = false,

     this.applyStatus = false,
  });

  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final AnnotationInputKindEnum kind;



  @JsonKey(
    
    name: r'verdict',
    required: false,
    includeIfNull: false,
  )


  final AnnotationInputVerdictEnum? verdict;



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
    defaultValue: false,
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;



      /// Agent scope: a `rejected` verdict also dismisses the finding or intel event
  @JsonKey(
    defaultValue: false,
    name: r'applyStatus',
    required: false,
    includeIfNull: false,
  )


  final bool? applyStatus;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnnotationInput &&
      other.kind == kind &&
      other.verdict == verdict &&
      other.duplicateOfRef == duplicateOfRef &&
      other.body == body &&
      other.tags == tags &&
      other.private == private &&
      other.applyStatus == applyStatus;

    @override
    int get hashCode =>
        kind.hashCode +
        verdict.hashCode +
        duplicateOfRef.hashCode +
        body.hashCode +
        tags.hashCode +
        private.hashCode +
        applyStatus.hashCode;

  factory AnnotationInput.fromJson(Map<String, dynamic> json) => _$AnnotationInputFromJson(json);

  Map<String, dynamic> toJson() => _$AnnotationInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnnotationInputKindEnum {
@JsonValue(r'note')
note(r'note'),
@JsonValue(r'verdict')
verdict(r'verdict');

const AnnotationInputKindEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnnotationInputVerdictEnum {
@JsonValue(r'confirmed')
confirmed(r'confirmed'),
@JsonValue(r'rejected')
rejected(r'rejected'),
@JsonValue(r'needs-review')
needsReview(r'needs-review'),
@JsonValue(r'duplicate-of')
duplicateOf(r'duplicate-of');

const AnnotationInputVerdictEnum(this.value);

final String value;

@override
String toString() => value;
}



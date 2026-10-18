// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'annotation.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnnotationCWProxy {
  Annotation id(String id);

  Annotation ref(String? ref);

  Annotation targetRef(String targetRef);

  Annotation kind(AnnotationKindEnum kind);

  Annotation verdict(AnnotationVerdictEnum? verdict);

  Annotation duplicateOfRef(String? duplicateOfRef);

  Annotation body(String? body);

  Annotation tags(List<String>? tags);

  Annotation author(String author);

  Annotation private(bool private);

  Annotation statusApplied(bool? statusApplied);

  Annotation createdAt(DateTime createdAt);

  Annotation updatedAt(DateTime updatedAt);

  Annotation deletedAt(DateTime? deletedAt);

  Annotation parentId(String? parentId);

  Annotation mentions(List<String>? mentions);

  Annotation droppedMentions(List<String>? droppedMentions);

  Annotation resolved(bool? resolved);

  Annotation editedAt(DateTime? editedAt);

  Annotation tlp(Tlp? tlp);

  Annotation visibility(Visibility? visibility);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Annotation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Annotation(...).copyWith(id: 12, name: "My name")
  /// ```
  Annotation call({
    String id,
    String? ref,
    String targetRef,
    AnnotationKindEnum kind,
    AnnotationVerdictEnum? verdict,
    String? duplicateOfRef,
    String? body,
    List<String>? tags,
    String author,
    bool private,
    bool? statusApplied,
    DateTime createdAt,
    DateTime updatedAt,
    DateTime? deletedAt,
    String? parentId,
    List<String>? mentions,
    List<String>? droppedMentions,
    bool? resolved,
    DateTime? editedAt,
    Tlp? tlp,
    Visibility? visibility,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnnotation.copyWith(...)` or call `instanceOfAnnotation.copyWith.fieldName(value)` for a single field.
class _$AnnotationCWProxyImpl implements _$AnnotationCWProxy {
  const _$AnnotationCWProxyImpl(this._value);

  final Annotation _value;

  @override
  Annotation id(String id) => call(id: id);

  @override
  Annotation ref(String? ref) => call(ref: ref);

  @override
  Annotation targetRef(String targetRef) => call(targetRef: targetRef);

  @override
  Annotation kind(AnnotationKindEnum kind) => call(kind: kind);

  @override
  Annotation verdict(AnnotationVerdictEnum? verdict) => call(verdict: verdict);

  @override
  Annotation duplicateOfRef(String? duplicateOfRef) =>
      call(duplicateOfRef: duplicateOfRef);

  @override
  Annotation body(String? body) => call(body: body);

  @override
  Annotation tags(List<String>? tags) => call(tags: tags);

  @override
  Annotation author(String author) => call(author: author);

  @override
  Annotation private(bool private) => call(private: private);

  @override
  Annotation statusApplied(bool? statusApplied) =>
      call(statusApplied: statusApplied);

  @override
  Annotation createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Annotation updatedAt(DateTime updatedAt) => call(updatedAt: updatedAt);

  @override
  Annotation deletedAt(DateTime? deletedAt) => call(deletedAt: deletedAt);

  @override
  Annotation parentId(String? parentId) => call(parentId: parentId);

  @override
  Annotation mentions(List<String>? mentions) => call(mentions: mentions);

  @override
  Annotation droppedMentions(List<String>? droppedMentions) =>
      call(droppedMentions: droppedMentions);

  @override
  Annotation resolved(bool? resolved) => call(resolved: resolved);

  @override
  Annotation editedAt(DateTime? editedAt) => call(editedAt: editedAt);

  @override
  Annotation tlp(Tlp? tlp) => call(tlp: tlp);

  @override
  Annotation visibility(Visibility? visibility) => call(visibility: visibility);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Annotation(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Annotation(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Annotation call({
    Object? id = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? targetRef = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? verdict = const $CopyWithPlaceholder(),
    Object? duplicateOfRef = const $CopyWithPlaceholder(),
    Object? body = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? author = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? statusApplied = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? deletedAt = const $CopyWithPlaceholder(),
    Object? parentId = const $CopyWithPlaceholder(),
    Object? mentions = const $CopyWithPlaceholder(),
    Object? droppedMentions = const $CopyWithPlaceholder(),
    Object? resolved = const $CopyWithPlaceholder(),
    Object? editedAt = const $CopyWithPlaceholder(),
    Object? tlp = const $CopyWithPlaceholder(),
    Object? visibility = const $CopyWithPlaceholder(),
  }) {
    return Annotation(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
      targetRef: targetRef == const $CopyWithPlaceholder() || targetRef == null
          ? _value.targetRef
          // ignore: cast_nullable_to_non_nullable
          : targetRef as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as AnnotationKindEnum,
      verdict: verdict == const $CopyWithPlaceholder()
          ? _value.verdict
          // ignore: cast_nullable_to_non_nullable
          : verdict as AnnotationVerdictEnum?,
      duplicateOfRef: duplicateOfRef == const $CopyWithPlaceholder()
          ? _value.duplicateOfRef
          // ignore: cast_nullable_to_non_nullable
          : duplicateOfRef as String?,
      body: body == const $CopyWithPlaceholder()
          ? _value.body
          // ignore: cast_nullable_to_non_nullable
          : body as String?,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
      author: author == const $CopyWithPlaceholder() || author == null
          ? _value.author
          // ignore: cast_nullable_to_non_nullable
          : author as String,
      private: private == const $CopyWithPlaceholder() || private == null
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as bool,
      statusApplied: statusApplied == const $CopyWithPlaceholder()
          ? _value.statusApplied
          // ignore: cast_nullable_to_non_nullable
          : statusApplied as bool?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder() || updatedAt == null
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime,
      deletedAt: deletedAt == const $CopyWithPlaceholder()
          ? _value.deletedAt
          // ignore: cast_nullable_to_non_nullable
          : deletedAt as DateTime?,
      parentId: parentId == const $CopyWithPlaceholder()
          ? _value.parentId
          // ignore: cast_nullable_to_non_nullable
          : parentId as String?,
      mentions: mentions == const $CopyWithPlaceholder()
          ? _value.mentions
          // ignore: cast_nullable_to_non_nullable
          : mentions as List<String>?,
      droppedMentions: droppedMentions == const $CopyWithPlaceholder()
          ? _value.droppedMentions
          // ignore: cast_nullable_to_non_nullable
          : droppedMentions as List<String>?,
      resolved: resolved == const $CopyWithPlaceholder()
          ? _value.resolved
          // ignore: cast_nullable_to_non_nullable
          : resolved as bool?,
      editedAt: editedAt == const $CopyWithPlaceholder()
          ? _value.editedAt
          // ignore: cast_nullable_to_non_nullable
          : editedAt as DateTime?,
      tlp: tlp == const $CopyWithPlaceholder()
          ? _value.tlp
          // ignore: cast_nullable_to_non_nullable
          : tlp as Tlp?,
      visibility: visibility == const $CopyWithPlaceholder()
          ? _value.visibility
          // ignore: cast_nullable_to_non_nullable
          : visibility as Visibility?,
    );
  }
}

extension $AnnotationCopyWith on Annotation {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnnotation.copyWith(...)` or `instanceOfAnnotation.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnnotationCWProxy get copyWith => _$AnnotationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Annotation _$AnnotationFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Annotation', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'targetRef',
      'kind',
      'author',
      'private',
      'createdAt',
      'updatedAt',
    ],
  );
  final val = Annotation(
    id: $checkedConvert('id', (v) => v as String),
    ref: $checkedConvert('ref', (v) => v as String?),
    targetRef: $checkedConvert('targetRef', (v) => v as String),
    kind: $checkedConvert(
      'kind',
      (v) => $enumDecode(_$AnnotationKindEnumEnumMap, v),
    ),
    verdict: $checkedConvert(
      'verdict',
      (v) => $enumDecodeNullable(_$AnnotationVerdictEnumEnumMap, v),
    ),
    duplicateOfRef: $checkedConvert('duplicateOfRef', (v) => v as String?),
    body: $checkedConvert('body', (v) => v as String?),
    tags: $checkedConvert(
      'tags',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    author: $checkedConvert('author', (v) => v as String),
    private: $checkedConvert('private', (v) => v as bool),
    statusApplied: $checkedConvert('statusApplied', (v) => v as bool?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert('updatedAt', (v) => DateTime.parse(v as String)),
    deletedAt: $checkedConvert(
      'deletedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    parentId: $checkedConvert('parentId', (v) => v as String?),
    mentions: $checkedConvert(
      'mentions',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    droppedMentions: $checkedConvert(
      'droppedMentions',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    resolved: $checkedConvert('resolved', (v) => v as bool?),
    editedAt: $checkedConvert(
      'editedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    tlp: $checkedConvert('tlp', (v) => $enumDecodeNullable(_$TlpEnumMap, v)),
    visibility: $checkedConvert(
      'visibility',
      (v) => $enumDecodeNullable(_$VisibilityEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$AnnotationToJson(Annotation instance) =>
    <String, dynamic>{
      'id': instance.id,
      'ref': ?instance.ref,
      'targetRef': instance.targetRef,
      'kind': _$AnnotationKindEnumEnumMap[instance.kind]!,
      'verdict': ?_$AnnotationVerdictEnumEnumMap[instance.verdict],
      'duplicateOfRef': ?instance.duplicateOfRef,
      'body': ?instance.body,
      'tags': ?instance.tags,
      'author': instance.author,
      'private': instance.private,
      'statusApplied': ?instance.statusApplied,
      'createdAt': instance.createdAt.toIso8601String(),
      'updatedAt': instance.updatedAt.toIso8601String(),
      'deletedAt': ?instance.deletedAt?.toIso8601String(),
      'parentId': ?instance.parentId,
      'mentions': ?instance.mentions,
      'droppedMentions': ?instance.droppedMentions,
      'resolved': ?instance.resolved,
      'editedAt': ?instance.editedAt?.toIso8601String(),
      'tlp': ?_$TlpEnumMap[instance.tlp],
      'visibility': ?_$VisibilityEnumMap[instance.visibility],
    };

const _$AnnotationKindEnumEnumMap = {
  AnnotationKindEnum.note: 'note',
  AnnotationKindEnum.verdict: 'verdict',
};

const _$AnnotationVerdictEnumEnumMap = {
  AnnotationVerdictEnum.confirmed: 'confirmed',
  AnnotationVerdictEnum.rejected: 'rejected',
  AnnotationVerdictEnum.needsReview: 'needs-review',
  AnnotationVerdictEnum.duplicateOf: 'duplicate-of',
};

const _$TlpEnumMap = {
  Tlp.CLEAR: 'CLEAR',
  Tlp.GREEN: 'GREEN',
  Tlp.AMBER: 'AMBER',
  Tlp.aMBERPlusSTRICT: 'AMBER+STRICT',
  Tlp.RED: 'RED',
};

const _$VisibilityEnumMap = {
  Visibility.private: 'private',
  Visibility.shared: 'shared',
  Visibility.instance: 'instance',
};

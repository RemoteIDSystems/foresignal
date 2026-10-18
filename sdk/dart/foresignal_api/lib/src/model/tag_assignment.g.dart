// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tag_assignment.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TagAssignmentCWProxy {
  TagAssignment tagId(String tagId);

  TagAssignment tag(Tag? tag);

  TagAssignment objectRef(String objectRef);

  TagAssignment assignedBy(PrincipalSummary? assignedBy);

  TagAssignment assignedAt(DateTime assignedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TagAssignment(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TagAssignment(...).copyWith(id: 12, name: "My name")
  /// ```
  TagAssignment call({
    String tagId,
    Tag? tag,
    String objectRef,
    PrincipalSummary? assignedBy,
    DateTime assignedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTagAssignment.copyWith(...)` or call `instanceOfTagAssignment.copyWith.fieldName(value)` for a single field.
class _$TagAssignmentCWProxyImpl implements _$TagAssignmentCWProxy {
  const _$TagAssignmentCWProxyImpl(this._value);

  final TagAssignment _value;

  @override
  TagAssignment tagId(String tagId) => call(tagId: tagId);

  @override
  TagAssignment tag(Tag? tag) => call(tag: tag);

  @override
  TagAssignment objectRef(String objectRef) => call(objectRef: objectRef);

  @override
  TagAssignment assignedBy(PrincipalSummary? assignedBy) =>
      call(assignedBy: assignedBy);

  @override
  TagAssignment assignedAt(DateTime assignedAt) => call(assignedAt: assignedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TagAssignment(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TagAssignment(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TagAssignment call({
    Object? tagId = const $CopyWithPlaceholder(),
    Object? tag = const $CopyWithPlaceholder(),
    Object? objectRef = const $CopyWithPlaceholder(),
    Object? assignedBy = const $CopyWithPlaceholder(),
    Object? assignedAt = const $CopyWithPlaceholder(),
  }) {
    return TagAssignment(
      tagId: tagId == const $CopyWithPlaceholder() || tagId == null
          ? _value.tagId
          // ignore: cast_nullable_to_non_nullable
          : tagId as String,
      tag: tag == const $CopyWithPlaceholder()
          ? _value.tag
          // ignore: cast_nullable_to_non_nullable
          : tag as Tag?,
      objectRef: objectRef == const $CopyWithPlaceholder() || objectRef == null
          ? _value.objectRef
          // ignore: cast_nullable_to_non_nullable
          : objectRef as String,
      assignedBy: assignedBy == const $CopyWithPlaceholder()
          ? _value.assignedBy
          // ignore: cast_nullable_to_non_nullable
          : assignedBy as PrincipalSummary?,
      assignedAt:
          assignedAt == const $CopyWithPlaceholder() || assignedAt == null
          ? _value.assignedAt
          // ignore: cast_nullable_to_non_nullable
          : assignedAt as DateTime,
    );
  }
}

extension $TagAssignmentCopyWith on TagAssignment {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTagAssignment.copyWith(...)` or `instanceOfTagAssignment.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TagAssignmentCWProxy get copyWith => _$TagAssignmentCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TagAssignment _$TagAssignmentFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TagAssignment', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['tagId', 'objectRef', 'assignedAt'],
      );
      final val = TagAssignment(
        tagId: $checkedConvert('tagId', (v) => v as String),
        tag: $checkedConvert(
          'tag',
          (v) => v == null ? null : Tag.fromJson(v as Map<String, dynamic>),
        ),
        objectRef: $checkedConvert('objectRef', (v) => v as String),
        assignedBy: $checkedConvert(
          'assignedBy',
          (v) => v == null
              ? null
              : PrincipalSummary.fromJson(v as Map<String, dynamic>),
        ),
        assignedAt: $checkedConvert(
          'assignedAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$TagAssignmentToJson(TagAssignment instance) =>
    <String, dynamic>{
      'tagId': instance.tagId,
      'tag': ?instance.tag?.toJson(),
      'objectRef': instance.objectRef,
      'assignedBy': ?instance.assignedBy?.toJson(),
      'assignedAt': instance.assignedAt.toIso8601String(),
    };

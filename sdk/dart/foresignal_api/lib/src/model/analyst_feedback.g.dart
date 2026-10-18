// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analyst_feedback.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalystFeedbackCWProxy {
  AnalystFeedback id(String id);

  AnalystFeedback target(AnalystFeedbackTargetEnum target);

  AnalystFeedback targetId(String targetId);

  AnalystFeedback rating(AnalystFeedbackRatingEnum rating);

  AnalystFeedback comment(String? comment);

  AnalystFeedback by(String by);

  AnalystFeedback at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystFeedback(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystFeedback(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalystFeedback call({
    String id,
    AnalystFeedbackTargetEnum target,
    String targetId,
    AnalystFeedbackRatingEnum rating,
    String? comment,
    String by,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalystFeedback.copyWith(...)` or call `instanceOfAnalystFeedback.copyWith.fieldName(value)` for a single field.
class _$AnalystFeedbackCWProxyImpl implements _$AnalystFeedbackCWProxy {
  const _$AnalystFeedbackCWProxyImpl(this._value);

  final AnalystFeedback _value;

  @override
  AnalystFeedback id(String id) => call(id: id);

  @override
  AnalystFeedback target(AnalystFeedbackTargetEnum target) =>
      call(target: target);

  @override
  AnalystFeedback targetId(String targetId) => call(targetId: targetId);

  @override
  AnalystFeedback rating(AnalystFeedbackRatingEnum rating) =>
      call(rating: rating);

  @override
  AnalystFeedback comment(String? comment) => call(comment: comment);

  @override
  AnalystFeedback by(String by) => call(by: by);

  @override
  AnalystFeedback at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystFeedback(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystFeedback(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalystFeedback call({
    Object? id = const $CopyWithPlaceholder(),
    Object? target = const $CopyWithPlaceholder(),
    Object? targetId = const $CopyWithPlaceholder(),
    Object? rating = const $CopyWithPlaceholder(),
    Object? comment = const $CopyWithPlaceholder(),
    Object? by = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return AnalystFeedback(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      target: target == const $CopyWithPlaceholder() || target == null
          ? _value.target
          // ignore: cast_nullable_to_non_nullable
          : target as AnalystFeedbackTargetEnum,
      targetId: targetId == const $CopyWithPlaceholder() || targetId == null
          ? _value.targetId
          // ignore: cast_nullable_to_non_nullable
          : targetId as String,
      rating: rating == const $CopyWithPlaceholder() || rating == null
          ? _value.rating
          // ignore: cast_nullable_to_non_nullable
          : rating as AnalystFeedbackRatingEnum,
      comment: comment == const $CopyWithPlaceholder()
          ? _value.comment
          // ignore: cast_nullable_to_non_nullable
          : comment as String?,
      by: by == const $CopyWithPlaceholder() || by == null
          ? _value.by
          // ignore: cast_nullable_to_non_nullable
          : by as String,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $AnalystFeedbackCopyWith on AnalystFeedback {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalystFeedback.copyWith(...)` or `instanceOfAnalystFeedback.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalystFeedbackCWProxy get copyWith => _$AnalystFeedbackCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalystFeedback _$AnalystFeedbackFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AnalystFeedback', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['id', 'target', 'targetId', 'rating', 'by', 'at'],
      );
      final val = AnalystFeedback(
        id: $checkedConvert('id', (v) => v as String),
        target: $checkedConvert(
          'target',
          (v) => $enumDecode(_$AnalystFeedbackTargetEnumEnumMap, v),
        ),
        targetId: $checkedConvert('targetId', (v) => v as String),
        rating: $checkedConvert(
          'rating',
          (v) => $enumDecode(_$AnalystFeedbackRatingEnumEnumMap, v),
        ),
        comment: $checkedConvert('comment', (v) => v as String?),
        by: $checkedConvert('by', (v) => v as String),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$AnalystFeedbackToJson(AnalystFeedback instance) =>
    <String, dynamic>{
      'id': instance.id,
      'target': _$AnalystFeedbackTargetEnumEnumMap[instance.target]!,
      'targetId': instance.targetId,
      'rating': _$AnalystFeedbackRatingEnumEnumMap[instance.rating]!,
      'comment': ?instance.comment,
      'by': instance.by,
      'at': instance.at.toIso8601String(),
    };

const _$AnalystFeedbackTargetEnumEnumMap = {
  AnalystFeedbackTargetEnum.note: 'note',
  AnalystFeedbackTargetEnum.proposal: 'proposal',
};

const _$AnalystFeedbackRatingEnumEnumMap = {
  AnalystFeedbackRatingEnum.useful: 'useful',
  AnalystFeedbackRatingEnum.notUseful: 'not_useful',
  AnalystFeedbackRatingEnum.wrong: 'wrong',
};

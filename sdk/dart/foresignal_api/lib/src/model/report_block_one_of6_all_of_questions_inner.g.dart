// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'report_block_one_of6_all_of_questions_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReportBlockOneOf6AllOfQuestionsInnerCWProxy {
  ReportBlockOneOf6AllOfQuestionsInner questionRef(String questionRef);

  ReportBlockOneOf6AllOfQuestionsInner snapshot(Map<String, Object>? snapshot);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf6AllOfQuestionsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf6AllOfQuestionsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ReportBlockOneOf6AllOfQuestionsInner call({
    String questionRef,
    Map<String, Object>? snapshot,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReportBlockOneOf6AllOfQuestionsInner.copyWith(...)` or call `instanceOfReportBlockOneOf6AllOfQuestionsInner.copyWith.fieldName(value)` for a single field.
class _$ReportBlockOneOf6AllOfQuestionsInnerCWProxyImpl
    implements _$ReportBlockOneOf6AllOfQuestionsInnerCWProxy {
  const _$ReportBlockOneOf6AllOfQuestionsInnerCWProxyImpl(this._value);

  final ReportBlockOneOf6AllOfQuestionsInner _value;

  @override
  ReportBlockOneOf6AllOfQuestionsInner questionRef(String questionRef) =>
      call(questionRef: questionRef);

  @override
  ReportBlockOneOf6AllOfQuestionsInner snapshot(
    Map<String, Object>? snapshot,
  ) => call(snapshot: snapshot);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReportBlockOneOf6AllOfQuestionsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReportBlockOneOf6AllOfQuestionsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReportBlockOneOf6AllOfQuestionsInner call({
    Object? questionRef = const $CopyWithPlaceholder(),
    Object? snapshot = const $CopyWithPlaceholder(),
  }) {
    return ReportBlockOneOf6AllOfQuestionsInner(
      questionRef:
          questionRef == const $CopyWithPlaceholder() || questionRef == null
          ? _value.questionRef
          // ignore: cast_nullable_to_non_nullable
          : questionRef as String,
      snapshot: snapshot == const $CopyWithPlaceholder()
          ? _value.snapshot
          // ignore: cast_nullable_to_non_nullable
          : snapshot as Map<String, Object>?,
    );
  }
}

extension $ReportBlockOneOf6AllOfQuestionsInnerCopyWith
    on ReportBlockOneOf6AllOfQuestionsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReportBlockOneOf6AllOfQuestionsInner.copyWith(...)` or `instanceOfReportBlockOneOf6AllOfQuestionsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReportBlockOneOf6AllOfQuestionsInnerCWProxy get copyWith =>
      _$ReportBlockOneOf6AllOfQuestionsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReportBlockOneOf6AllOfQuestionsInner
_$ReportBlockOneOf6AllOfQuestionsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReportBlockOneOf6AllOfQuestionsInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['questionRef']);
      final val = ReportBlockOneOf6AllOfQuestionsInner(
        questionRef: $checkedConvert('questionRef', (v) => v as String),
        snapshot: $checkedConvert(
          'snapshot',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$ReportBlockOneOf6AllOfQuestionsInnerToJson(
  ReportBlockOneOf6AllOfQuestionsInner instance,
) => <String, dynamic>{
  'questionRef': instance.questionRef,
  'snapshot': ?instance.snapshot,
};

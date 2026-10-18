// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'question_annulment.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$QuestionAnnulmentCWProxy {
  QuestionAnnulment code(QuestionAnnulmentCodeEnum code);

  QuestionAnnulment reason(String reason);

  QuestionAnnulment at(DateTime at);

  QuestionAnnulment by(String by);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `QuestionAnnulment(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// QuestionAnnulment(...).copyWith(id: 12, name: "My name")
  /// ```
  QuestionAnnulment call({
    QuestionAnnulmentCodeEnum code,
    String reason,
    DateTime at,
    String by,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfQuestionAnnulment.copyWith(...)` or call `instanceOfQuestionAnnulment.copyWith.fieldName(value)` for a single field.
class _$QuestionAnnulmentCWProxyImpl implements _$QuestionAnnulmentCWProxy {
  const _$QuestionAnnulmentCWProxyImpl(this._value);

  final QuestionAnnulment _value;

  @override
  QuestionAnnulment code(QuestionAnnulmentCodeEnum code) => call(code: code);

  @override
  QuestionAnnulment reason(String reason) => call(reason: reason);

  @override
  QuestionAnnulment at(DateTime at) => call(at: at);

  @override
  QuestionAnnulment by(String by) => call(by: by);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `QuestionAnnulment(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// QuestionAnnulment(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  QuestionAnnulment call({
    Object? code = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? by = const $CopyWithPlaceholder(),
  }) {
    return QuestionAnnulment(
      code: code == const $CopyWithPlaceholder() || code == null
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as QuestionAnnulmentCodeEnum,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      by: by == const $CopyWithPlaceholder() || by == null
          ? _value.by
          // ignore: cast_nullable_to_non_nullable
          : by as String,
    );
  }
}

extension $QuestionAnnulmentCopyWith on QuestionAnnulment {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfQuestionAnnulment.copyWith(...)` or `instanceOfQuestionAnnulment.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$QuestionAnnulmentCWProxy get copyWith =>
      _$QuestionAnnulmentCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

QuestionAnnulment _$QuestionAnnulmentFromJson(Map<String, dynamic> json) =>
    $checkedCreate('QuestionAnnulment', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['code', 'reason', 'at', 'by']);
      final val = QuestionAnnulment(
        code: $checkedConvert(
          'code',
          (v) => $enumDecode(_$QuestionAnnulmentCodeEnumEnumMap, v),
        ),
        reason: $checkedConvert('reason', (v) => v as String),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
        by: $checkedConvert('by', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$QuestionAnnulmentToJson(QuestionAnnulment instance) =>
    <String, dynamic>{
      'code': _$QuestionAnnulmentCodeEnumEnumMap[instance.code]!,
      'reason': instance.reason,
      'at': instance.at.toIso8601String(),
      'by': instance.by,
    };

const _$QuestionAnnulmentCodeEnumEnumMap = {
  QuestionAnnulmentCodeEnum.LOW_DETECTABILITY: 'LOW_DETECTABILITY',
  QuestionAnnulmentCodeEnum.COVERAGE_GAP: 'COVERAGE_GAP',
  QuestionAnnulmentCodeEnum.OPERATOR: 'OPERATOR',
};

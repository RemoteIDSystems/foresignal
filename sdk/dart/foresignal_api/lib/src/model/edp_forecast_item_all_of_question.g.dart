// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_forecast_item_all_of_question.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpForecastItemAllOfQuestionCWProxy {
  EdpForecastItemAllOfQuestion externalId(String externalId);

  EdpForecastItemAllOfQuestion title(String title);

  EdpForecastItemAllOfQuestion closeTime(DateTime? closeTime);

  EdpForecastItemAllOfQuestion resolutionCriteria(String? resolutionCriteria);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpForecastItemAllOfQuestion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpForecastItemAllOfQuestion(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpForecastItemAllOfQuestion call({
    String externalId,
    String title,
    DateTime? closeTime,
    String? resolutionCriteria,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpForecastItemAllOfQuestion.copyWith(...)` or call `instanceOfEdpForecastItemAllOfQuestion.copyWith.fieldName(value)` for a single field.
class _$EdpForecastItemAllOfQuestionCWProxyImpl
    implements _$EdpForecastItemAllOfQuestionCWProxy {
  const _$EdpForecastItemAllOfQuestionCWProxyImpl(this._value);

  final EdpForecastItemAllOfQuestion _value;

  @override
  EdpForecastItemAllOfQuestion externalId(String externalId) =>
      call(externalId: externalId);

  @override
  EdpForecastItemAllOfQuestion title(String title) => call(title: title);

  @override
  EdpForecastItemAllOfQuestion closeTime(DateTime? closeTime) =>
      call(closeTime: closeTime);

  @override
  EdpForecastItemAllOfQuestion resolutionCriteria(String? resolutionCriteria) =>
      call(resolutionCriteria: resolutionCriteria);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpForecastItemAllOfQuestion(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpForecastItemAllOfQuestion(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpForecastItemAllOfQuestion call({
    Object? externalId = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? closeTime = const $CopyWithPlaceholder(),
    Object? resolutionCriteria = const $CopyWithPlaceholder(),
  }) {
    return EdpForecastItemAllOfQuestion(
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      closeTime: closeTime == const $CopyWithPlaceholder()
          ? _value.closeTime
          // ignore: cast_nullable_to_non_nullable
          : closeTime as DateTime?,
      resolutionCriteria: resolutionCriteria == const $CopyWithPlaceholder()
          ? _value.resolutionCriteria
          // ignore: cast_nullable_to_non_nullable
          : resolutionCriteria as String?,
    );
  }
}

extension $EdpForecastItemAllOfQuestionCopyWith
    on EdpForecastItemAllOfQuestion {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpForecastItemAllOfQuestion.copyWith(...)` or `instanceOfEdpForecastItemAllOfQuestion.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpForecastItemAllOfQuestionCWProxy get copyWith =>
      _$EdpForecastItemAllOfQuestionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpForecastItemAllOfQuestion _$EdpForecastItemAllOfQuestionFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EdpForecastItemAllOfQuestion', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['externalId', 'title']);
  final val = EdpForecastItemAllOfQuestion(
    externalId: $checkedConvert('externalId', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String),
    closeTime: $checkedConvert(
      'closeTime',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    resolutionCriteria: $checkedConvert(
      'resolutionCriteria',
      (v) => v as String?,
    ),
  );
  return val;
});

Map<String, dynamic> _$EdpForecastItemAllOfQuestionToJson(
  EdpForecastItemAllOfQuestion instance,
) => <String, dynamic>{
  'externalId': instance.externalId,
  'title': instance.title,
  'closeTime': ?instance.closeTime?.toIso8601String(),
  'resolutionCriteria': ?instance.resolutionCriteria,
};

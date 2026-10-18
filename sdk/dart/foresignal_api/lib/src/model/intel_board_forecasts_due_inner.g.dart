// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_board_forecasts_due_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelBoardForecastsDueInnerCWProxy {
  IntelBoardForecastsDueInner questionId(String questionId);

  IntelBoardForecastsDueInner title(String title);

  IntelBoardForecastsDueInner dueAt(DateTime dueAt);

  IntelBoardForecastsDueInner probability(num? probability);

  IntelBoardForecastsDueInner estimativeTerm(IcdTerm? estimativeTerm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelBoardForecastsDueInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelBoardForecastsDueInner(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelBoardForecastsDueInner call({
    String questionId,
    String title,
    DateTime dueAt,
    num? probability,
    IcdTerm? estimativeTerm,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelBoardForecastsDueInner.copyWith(...)` or call `instanceOfIntelBoardForecastsDueInner.copyWith.fieldName(value)` for a single field.
class _$IntelBoardForecastsDueInnerCWProxyImpl
    implements _$IntelBoardForecastsDueInnerCWProxy {
  const _$IntelBoardForecastsDueInnerCWProxyImpl(this._value);

  final IntelBoardForecastsDueInner _value;

  @override
  IntelBoardForecastsDueInner questionId(String questionId) =>
      call(questionId: questionId);

  @override
  IntelBoardForecastsDueInner title(String title) => call(title: title);

  @override
  IntelBoardForecastsDueInner dueAt(DateTime dueAt) => call(dueAt: dueAt);

  @override
  IntelBoardForecastsDueInner probability(num? probability) =>
      call(probability: probability);

  @override
  IntelBoardForecastsDueInner estimativeTerm(IcdTerm? estimativeTerm) =>
      call(estimativeTerm: estimativeTerm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelBoardForecastsDueInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelBoardForecastsDueInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelBoardForecastsDueInner call({
    Object? questionId = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? dueAt = const $CopyWithPlaceholder(),
    Object? probability = const $CopyWithPlaceholder(),
    Object? estimativeTerm = const $CopyWithPlaceholder(),
  }) {
    return IntelBoardForecastsDueInner(
      questionId:
          questionId == const $CopyWithPlaceholder() || questionId == null
          ? _value.questionId
          // ignore: cast_nullable_to_non_nullable
          : questionId as String,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      dueAt: dueAt == const $CopyWithPlaceholder() || dueAt == null
          ? _value.dueAt
          // ignore: cast_nullable_to_non_nullable
          : dueAt as DateTime,
      probability: probability == const $CopyWithPlaceholder()
          ? _value.probability
          // ignore: cast_nullable_to_non_nullable
          : probability as num?,
      estimativeTerm: estimativeTerm == const $CopyWithPlaceholder()
          ? _value.estimativeTerm
          // ignore: cast_nullable_to_non_nullable
          : estimativeTerm as IcdTerm?,
    );
  }
}

extension $IntelBoardForecastsDueInnerCopyWith on IntelBoardForecastsDueInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelBoardForecastsDueInner.copyWith(...)` or `instanceOfIntelBoardForecastsDueInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelBoardForecastsDueInnerCWProxy get copyWith =>
      _$IntelBoardForecastsDueInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelBoardForecastsDueInner _$IntelBoardForecastsDueInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelBoardForecastsDueInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['questionId', 'title', 'dueAt']);
  final val = IntelBoardForecastsDueInner(
    questionId: $checkedConvert('questionId', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String),
    dueAt: $checkedConvert('dueAt', (v) => DateTime.parse(v as String)),
    probability: $checkedConvert('probability', (v) => v as num?),
    estimativeTerm: $checkedConvert(
      'estimativeTerm',
      (v) => $enumDecodeNullable(_$IcdTermEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$IntelBoardForecastsDueInnerToJson(
  IntelBoardForecastsDueInner instance,
) => <String, dynamic>{
  'questionId': instance.questionId,
  'title': instance.title,
  'dueAt': instance.dueAt.toIso8601String(),
  'probability': ?instance.probability,
  'estimativeTerm': ?_$IcdTermEnumMap[instance.estimativeTerm],
};

const _$IcdTermEnumMap = {
  IcdTerm.almostNoChance: 'almost no chance',
  IcdTerm.veryUnlikely: 'very unlikely',
  IcdTerm.unlikely: 'unlikely',
  IcdTerm.roughlyEvenChance: 'roughly even chance',
  IcdTerm.likely: 'likely',
  IcdTerm.veryLikely: 'very likely',
  IcdTerm.almostCertain: 'almost certain',
};

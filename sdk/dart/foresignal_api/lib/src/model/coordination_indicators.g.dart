// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'coordination_indicators.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CoordinationIndicatorsCWProxy {
  CoordinationIndicators narrativeId(String narrativeId);

  CoordinationIndicators window(TimeRange window);

  CoordinationIndicators synchrony(CoordinationIndicator synchrony);

  CoordinationIndicators textDuplication(CoordinationIndicator textDuplication);

  CoordinationIndicators accountAgeSkew(CoordinationIndicator accountAgeSkew);

  CoordinationIndicators coRepostBursts(CoordinationIndicator coRepostBursts);

  CoordinationIndicators significantCount(int significantCount);

  CoordinationIndicators findingId(String? findingId);

  CoordinationIndicators statement(String? statement);

  CoordinationIndicators detector(DetectorRef detector);

  CoordinationIndicators computedAt(DateTime computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CoordinationIndicators(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CoordinationIndicators(...).copyWith(id: 12, name: "My name")
  /// ```
  CoordinationIndicators call({
    String narrativeId,
    TimeRange window,
    CoordinationIndicator synchrony,
    CoordinationIndicator textDuplication,
    CoordinationIndicator accountAgeSkew,
    CoordinationIndicator coRepostBursts,
    int significantCount,
    String? findingId,
    String? statement,
    DetectorRef detector,
    DateTime computedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCoordinationIndicators.copyWith(...)` or call `instanceOfCoordinationIndicators.copyWith.fieldName(value)` for a single field.
class _$CoordinationIndicatorsCWProxyImpl
    implements _$CoordinationIndicatorsCWProxy {
  const _$CoordinationIndicatorsCWProxyImpl(this._value);

  final CoordinationIndicators _value;

  @override
  CoordinationIndicators narrativeId(String narrativeId) =>
      call(narrativeId: narrativeId);

  @override
  CoordinationIndicators window(TimeRange window) => call(window: window);

  @override
  CoordinationIndicators synchrony(CoordinationIndicator synchrony) =>
      call(synchrony: synchrony);

  @override
  CoordinationIndicators textDuplication(
    CoordinationIndicator textDuplication,
  ) => call(textDuplication: textDuplication);

  @override
  CoordinationIndicators accountAgeSkew(CoordinationIndicator accountAgeSkew) =>
      call(accountAgeSkew: accountAgeSkew);

  @override
  CoordinationIndicators coRepostBursts(CoordinationIndicator coRepostBursts) =>
      call(coRepostBursts: coRepostBursts);

  @override
  CoordinationIndicators significantCount(int significantCount) =>
      call(significantCount: significantCount);

  @override
  CoordinationIndicators findingId(String? findingId) =>
      call(findingId: findingId);

  @override
  CoordinationIndicators statement(String? statement) =>
      call(statement: statement);

  @override
  CoordinationIndicators detector(DetectorRef detector) =>
      call(detector: detector);

  @override
  CoordinationIndicators computedAt(DateTime computedAt) =>
      call(computedAt: computedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CoordinationIndicators(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CoordinationIndicators(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CoordinationIndicators call({
    Object? narrativeId = const $CopyWithPlaceholder(),
    Object? window = const $CopyWithPlaceholder(),
    Object? synchrony = const $CopyWithPlaceholder(),
    Object? textDuplication = const $CopyWithPlaceholder(),
    Object? accountAgeSkew = const $CopyWithPlaceholder(),
    Object? coRepostBursts = const $CopyWithPlaceholder(),
    Object? significantCount = const $CopyWithPlaceholder(),
    Object? findingId = const $CopyWithPlaceholder(),
    Object? statement = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? computedAt = const $CopyWithPlaceholder(),
  }) {
    return CoordinationIndicators(
      narrativeId:
          narrativeId == const $CopyWithPlaceholder() || narrativeId == null
          ? _value.narrativeId
          // ignore: cast_nullable_to_non_nullable
          : narrativeId as String,
      window: window == const $CopyWithPlaceholder() || window == null
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as TimeRange,
      synchrony: synchrony == const $CopyWithPlaceholder() || synchrony == null
          ? _value.synchrony
          // ignore: cast_nullable_to_non_nullable
          : synchrony as CoordinationIndicator,
      textDuplication:
          textDuplication == const $CopyWithPlaceholder() ||
              textDuplication == null
          ? _value.textDuplication
          // ignore: cast_nullable_to_non_nullable
          : textDuplication as CoordinationIndicator,
      accountAgeSkew:
          accountAgeSkew == const $CopyWithPlaceholder() ||
              accountAgeSkew == null
          ? _value.accountAgeSkew
          // ignore: cast_nullable_to_non_nullable
          : accountAgeSkew as CoordinationIndicator,
      coRepostBursts:
          coRepostBursts == const $CopyWithPlaceholder() ||
              coRepostBursts == null
          ? _value.coRepostBursts
          // ignore: cast_nullable_to_non_nullable
          : coRepostBursts as CoordinationIndicator,
      significantCount:
          significantCount == const $CopyWithPlaceholder() ||
              significantCount == null
          ? _value.significantCount
          // ignore: cast_nullable_to_non_nullable
          : significantCount as int,
      findingId: findingId == const $CopyWithPlaceholder()
          ? _value.findingId
          // ignore: cast_nullable_to_non_nullable
          : findingId as String?,
      statement: statement == const $CopyWithPlaceholder()
          ? _value.statement
          // ignore: cast_nullable_to_non_nullable
          : statement as String?,
      detector: detector == const $CopyWithPlaceholder() || detector == null
          ? _value.detector
          // ignore: cast_nullable_to_non_nullable
          : detector as DetectorRef,
      computedAt:
          computedAt == const $CopyWithPlaceholder() || computedAt == null
          ? _value.computedAt
          // ignore: cast_nullable_to_non_nullable
          : computedAt as DateTime,
    );
  }
}

extension $CoordinationIndicatorsCopyWith on CoordinationIndicators {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCoordinationIndicators.copyWith(...)` or `instanceOfCoordinationIndicators.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CoordinationIndicatorsCWProxy get copyWith =>
      _$CoordinationIndicatorsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CoordinationIndicators _$CoordinationIndicatorsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CoordinationIndicators', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'narrativeId',
      'window',
      'synchrony',
      'textDuplication',
      'accountAgeSkew',
      'coRepostBursts',
      'significantCount',
      'detector',
      'computedAt',
    ],
  );
  final val = CoordinationIndicators(
    narrativeId: $checkedConvert('narrativeId', (v) => v as String),
    window: $checkedConvert(
      'window',
      (v) => TimeRange.fromJson(v as Map<String, dynamic>),
    ),
    synchrony: $checkedConvert(
      'synchrony',
      (v) => CoordinationIndicator.fromJson(v as Map<String, dynamic>),
    ),
    textDuplication: $checkedConvert(
      'textDuplication',
      (v) => CoordinationIndicator.fromJson(v as Map<String, dynamic>),
    ),
    accountAgeSkew: $checkedConvert(
      'accountAgeSkew',
      (v) => CoordinationIndicator.fromJson(v as Map<String, dynamic>),
    ),
    coRepostBursts: $checkedConvert(
      'coRepostBursts',
      (v) => CoordinationIndicator.fromJson(v as Map<String, dynamic>),
    ),
    significantCount: $checkedConvert(
      'significantCount',
      (v) => (v as num).toInt(),
    ),
    findingId: $checkedConvert('findingId', (v) => v as String?),
    statement: $checkedConvert('statement', (v) => v as String?),
    detector: $checkedConvert(
      'detector',
      (v) => DetectorRef.fromJson(v as Map<String, dynamic>),
    ),
    computedAt: $checkedConvert(
      'computedAt',
      (v) => DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$CoordinationIndicatorsToJson(
  CoordinationIndicators instance,
) => <String, dynamic>{
  'narrativeId': instance.narrativeId,
  'window': instance.window.toJson(),
  'synchrony': instance.synchrony.toJson(),
  'textDuplication': instance.textDuplication.toJson(),
  'accountAgeSkew': instance.accountAgeSkew.toJson(),
  'coRepostBursts': instance.coRepostBursts.toJson(),
  'significantCount': instance.significantCount,
  'findingId': ?instance.findingId,
  'statement': ?instance.statement,
  'detector': instance.detector.toJson(),
  'computedAt': instance.computedAt.toIso8601String(),
};

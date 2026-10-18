// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precision_components_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecisionComponentsInnerCWProxy {
  PrecisionComponentsInner component(
    PrecisionComponentsInnerComponentEnum component,
  );

  PrecisionComponentsInner key(String key);

  PrecisionComponentsInner confirmed(int confirmed);

  PrecisionComponentsInner rejected(int rejected);

  PrecisionComponentsInner duplicate(int? duplicate);

  PrecisionComponentsInner needsReview(int? needsReview);

  PrecisionComponentsInner labelled(int labelled);

  PrecisionComponentsInner precision(num? precision);

  PrecisionComponentsInner interval(ImpactRateInterval? interval);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecisionComponentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecisionComponentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecisionComponentsInner call({
    PrecisionComponentsInnerComponentEnum component,
    String key,
    int confirmed,
    int rejected,
    int? duplicate,
    int? needsReview,
    int labelled,
    num? precision,
    ImpactRateInterval? interval,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecisionComponentsInner.copyWith(...)` or call `instanceOfPrecisionComponentsInner.copyWith.fieldName(value)` for a single field.
class _$PrecisionComponentsInnerCWProxyImpl
    implements _$PrecisionComponentsInnerCWProxy {
  const _$PrecisionComponentsInnerCWProxyImpl(this._value);

  final PrecisionComponentsInner _value;

  @override
  PrecisionComponentsInner component(
    PrecisionComponentsInnerComponentEnum component,
  ) => call(component: component);

  @override
  PrecisionComponentsInner key(String key) => call(key: key);

  @override
  PrecisionComponentsInner confirmed(int confirmed) =>
      call(confirmed: confirmed);

  @override
  PrecisionComponentsInner rejected(int rejected) => call(rejected: rejected);

  @override
  PrecisionComponentsInner duplicate(int? duplicate) =>
      call(duplicate: duplicate);

  @override
  PrecisionComponentsInner needsReview(int? needsReview) =>
      call(needsReview: needsReview);

  @override
  PrecisionComponentsInner labelled(int labelled) => call(labelled: labelled);

  @override
  PrecisionComponentsInner precision(num? precision) =>
      call(precision: precision);

  @override
  PrecisionComponentsInner interval(ImpactRateInterval? interval) =>
      call(interval: interval);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecisionComponentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecisionComponentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecisionComponentsInner call({
    Object? component = const $CopyWithPlaceholder(),
    Object? key = const $CopyWithPlaceholder(),
    Object? confirmed = const $CopyWithPlaceholder(),
    Object? rejected = const $CopyWithPlaceholder(),
    Object? duplicate = const $CopyWithPlaceholder(),
    Object? needsReview = const $CopyWithPlaceholder(),
    Object? labelled = const $CopyWithPlaceholder(),
    Object? precision = const $CopyWithPlaceholder(),
    Object? interval = const $CopyWithPlaceholder(),
  }) {
    return PrecisionComponentsInner(
      component: component == const $CopyWithPlaceholder() || component == null
          ? _value.component
          // ignore: cast_nullable_to_non_nullable
          : component as PrecisionComponentsInnerComponentEnum,
      key: key == const $CopyWithPlaceholder() || key == null
          ? _value.key
          // ignore: cast_nullable_to_non_nullable
          : key as String,
      confirmed: confirmed == const $CopyWithPlaceholder() || confirmed == null
          ? _value.confirmed
          // ignore: cast_nullable_to_non_nullable
          : confirmed as int,
      rejected: rejected == const $CopyWithPlaceholder() || rejected == null
          ? _value.rejected
          // ignore: cast_nullable_to_non_nullable
          : rejected as int,
      duplicate: duplicate == const $CopyWithPlaceholder()
          ? _value.duplicate
          // ignore: cast_nullable_to_non_nullable
          : duplicate as int?,
      needsReview: needsReview == const $CopyWithPlaceholder()
          ? _value.needsReview
          // ignore: cast_nullable_to_non_nullable
          : needsReview as int?,
      labelled: labelled == const $CopyWithPlaceholder() || labelled == null
          ? _value.labelled
          // ignore: cast_nullable_to_non_nullable
          : labelled as int,
      precision: precision == const $CopyWithPlaceholder()
          ? _value.precision
          // ignore: cast_nullable_to_non_nullable
          : precision as num?,
      interval: interval == const $CopyWithPlaceholder()
          ? _value.interval
          // ignore: cast_nullable_to_non_nullable
          : interval as ImpactRateInterval?,
    );
  }
}

extension $PrecisionComponentsInnerCopyWith on PrecisionComponentsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecisionComponentsInner.copyWith(...)` or `instanceOfPrecisionComponentsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecisionComponentsInnerCWProxy get copyWith =>
      _$PrecisionComponentsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecisionComponentsInner _$PrecisionComponentsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecisionComponentsInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'component',
      'key',
      'confirmed',
      'rejected',
      'labelled',
    ],
  );
  final val = PrecisionComponentsInner(
    component: $checkedConvert(
      'component',
      (v) => $enumDecode(_$PrecisionComponentsInnerComponentEnumEnumMap, v),
    ),
    key: $checkedConvert('key', (v) => v as String),
    confirmed: $checkedConvert('confirmed', (v) => (v as num).toInt()),
    rejected: $checkedConvert('rejected', (v) => (v as num).toInt()),
    duplicate: $checkedConvert('duplicate', (v) => (v as num?)?.toInt()),
    needsReview: $checkedConvert('needsReview', (v) => (v as num?)?.toInt()),
    labelled: $checkedConvert('labelled', (v) => (v as num).toInt()),
    precision: $checkedConvert('precision', (v) => v as num?),
    interval: $checkedConvert(
      'interval',
      (v) => v == null
          ? null
          : ImpactRateInterval.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$PrecisionComponentsInnerToJson(
  PrecisionComponentsInner instance,
) => <String, dynamic>{
  'component':
      _$PrecisionComponentsInnerComponentEnumEnumMap[instance.component]!,
  'key': instance.key,
  'confirmed': instance.confirmed,
  'rejected': instance.rejected,
  'duplicate': ?instance.duplicate,
  'needsReview': ?instance.needsReview,
  'labelled': instance.labelled,
  'precision': ?instance.precision,
  'interval': ?instance.interval?.toJson(),
};

const _$PrecisionComponentsInnerComponentEnumEnumMap = {
  PrecisionComponentsInnerComponentEnum.finding: 'finding',
  PrecisionComponentsInnerComponentEnum.intelEvent: 'intel-event',
  PrecisionComponentsInnerComponentEnum.situation: 'situation',
  PrecisionComponentsInnerComponentEnum.precursorActive: 'precursor-active',
};

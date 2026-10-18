// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'information_value_indicators_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InformationValueIndicatorsInnerCWProxy {
  InformationValueIndicatorsInner indicatorId(String indicatorId);

  InformationValueIndicatorsInner description(String? description);

  InformationValueIndicatorsInner signalFamily(String? signalFamily);

  InformationValueIndicatorsInner state(
    InformationValueIndicatorsInnerStateEnum? state,
  );

  InformationValueIndicatorsInner rank(int rank);

  InformationValueIndicatorsInner eig(num eig);

  InformationValueIndicatorsInner potentialEig(num potentialEig);

  InformationValueIndicatorsInner pDetect(num? pDetect);

  InformationValueIndicatorsInner pObserved(num? pObserved);

  InformationValueIndicatorsInner collectionGap(bool? collectionGap);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InformationValueIndicatorsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InformationValueIndicatorsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  InformationValueIndicatorsInner call({
    String indicatorId,
    String? description,
    String? signalFamily,
    InformationValueIndicatorsInnerStateEnum? state,
    int rank,
    num eig,
    num potentialEig,
    num? pDetect,
    num? pObserved,
    bool? collectionGap,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInformationValueIndicatorsInner.copyWith(...)` or call `instanceOfInformationValueIndicatorsInner.copyWith.fieldName(value)` for a single field.
class _$InformationValueIndicatorsInnerCWProxyImpl
    implements _$InformationValueIndicatorsInnerCWProxy {
  const _$InformationValueIndicatorsInnerCWProxyImpl(this._value);

  final InformationValueIndicatorsInner _value;

  @override
  InformationValueIndicatorsInner indicatorId(String indicatorId) =>
      call(indicatorId: indicatorId);

  @override
  InformationValueIndicatorsInner description(String? description) =>
      call(description: description);

  @override
  InformationValueIndicatorsInner signalFamily(String? signalFamily) =>
      call(signalFamily: signalFamily);

  @override
  InformationValueIndicatorsInner state(
    InformationValueIndicatorsInnerStateEnum? state,
  ) => call(state: state);

  @override
  InformationValueIndicatorsInner rank(int rank) => call(rank: rank);

  @override
  InformationValueIndicatorsInner eig(num eig) => call(eig: eig);

  @override
  InformationValueIndicatorsInner potentialEig(num potentialEig) =>
      call(potentialEig: potentialEig);

  @override
  InformationValueIndicatorsInner pDetect(num? pDetect) =>
      call(pDetect: pDetect);

  @override
  InformationValueIndicatorsInner pObserved(num? pObserved) =>
      call(pObserved: pObserved);

  @override
  InformationValueIndicatorsInner collectionGap(bool? collectionGap) =>
      call(collectionGap: collectionGap);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InformationValueIndicatorsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InformationValueIndicatorsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InformationValueIndicatorsInner call({
    Object? indicatorId = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? signalFamily = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? rank = const $CopyWithPlaceholder(),
    Object? eig = const $CopyWithPlaceholder(),
    Object? potentialEig = const $CopyWithPlaceholder(),
    Object? pDetect = const $CopyWithPlaceholder(),
    Object? pObserved = const $CopyWithPlaceholder(),
    Object? collectionGap = const $CopyWithPlaceholder(),
  }) {
    return InformationValueIndicatorsInner(
      indicatorId:
          indicatorId == const $CopyWithPlaceholder() || indicatorId == null
          ? _value.indicatorId
          // ignore: cast_nullable_to_non_nullable
          : indicatorId as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      signalFamily: signalFamily == const $CopyWithPlaceholder()
          ? _value.signalFamily
          // ignore: cast_nullable_to_non_nullable
          : signalFamily as String?,
      state: state == const $CopyWithPlaceholder()
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as InformationValueIndicatorsInnerStateEnum?,
      rank: rank == const $CopyWithPlaceholder() || rank == null
          ? _value.rank
          // ignore: cast_nullable_to_non_nullable
          : rank as int,
      eig: eig == const $CopyWithPlaceholder() || eig == null
          ? _value.eig
          // ignore: cast_nullable_to_non_nullable
          : eig as num,
      potentialEig:
          potentialEig == const $CopyWithPlaceholder() || potentialEig == null
          ? _value.potentialEig
          // ignore: cast_nullable_to_non_nullable
          : potentialEig as num,
      pDetect: pDetect == const $CopyWithPlaceholder()
          ? _value.pDetect
          // ignore: cast_nullable_to_non_nullable
          : pDetect as num?,
      pObserved: pObserved == const $CopyWithPlaceholder()
          ? _value.pObserved
          // ignore: cast_nullable_to_non_nullable
          : pObserved as num?,
      collectionGap: collectionGap == const $CopyWithPlaceholder()
          ? _value.collectionGap
          // ignore: cast_nullable_to_non_nullable
          : collectionGap as bool?,
    );
  }
}

extension $InformationValueIndicatorsInnerCopyWith
    on InformationValueIndicatorsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInformationValueIndicatorsInner.copyWith(...)` or `instanceOfInformationValueIndicatorsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InformationValueIndicatorsInnerCWProxy get copyWith =>
      _$InformationValueIndicatorsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InformationValueIndicatorsInner _$InformationValueIndicatorsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('InformationValueIndicatorsInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['indicatorId', 'rank', 'eig', 'potentialEig'],
  );
  final val = InformationValueIndicatorsInner(
    indicatorId: $checkedConvert('indicatorId', (v) => v as String),
    description: $checkedConvert('description', (v) => v as String?),
    signalFamily: $checkedConvert('signalFamily', (v) => v as String?),
    state: $checkedConvert(
      'state',
      (v) => $enumDecodeNullable(
        _$InformationValueIndicatorsInnerStateEnumEnumMap,
        v,
      ),
    ),
    rank: $checkedConvert('rank', (v) => (v as num).toInt()),
    eig: $checkedConvert('eig', (v) => v as num),
    potentialEig: $checkedConvert('potentialEig', (v) => v as num),
    pDetect: $checkedConvert('pDetect', (v) => v as num?),
    pObserved: $checkedConvert('pObserved', (v) => v as num?),
    collectionGap: $checkedConvert('collectionGap', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$InformationValueIndicatorsInnerToJson(
  InformationValueIndicatorsInner instance,
) => <String, dynamic>{
  'indicatorId': instance.indicatorId,
  'description': ?instance.description,
  'signalFamily': ?instance.signalFamily,
  'state': ?_$InformationValueIndicatorsInnerStateEnumEnumMap[instance.state],
  'rank': instance.rank,
  'eig': instance.eig,
  'potentialEig': instance.potentialEig,
  'pDetect': ?instance.pDetect,
  'pObserved': ?instance.pObserved,
  'collectionGap': ?instance.collectionGap,
};

const _$InformationValueIndicatorsInnerStateEnumEnumMap = {
  InformationValueIndicatorsInnerStateEnum.pending: 'pending',
  InformationValueIndicatorsInnerStateEnum.emerging: 'emerging',
  InformationValueIndicatorsInnerStateEnum.observed: 'observed',
  InformationValueIndicatorsInnerStateEnum.absent: 'absent',
  InformationValueIndicatorsInnerStateEnum.unknownCoverage: 'unknown-coverage',
  InformationValueIndicatorsInnerStateEnum.retired: 'retired',
  InformationValueIndicatorsInnerStateEnum.reported: 'reported',
};

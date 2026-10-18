// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'federation_ingest_result_dropped.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FederationIngestResultDroppedCWProxy {
  FederationIngestResultDropped loop(int? loop);

  FederationIngestResultDropped hops(int? hops);

  FederationIngestResultDropped dedup(int? dedup);

  FederationIngestResultDropped licence(int? licence);

  FederationIngestResultDropped private(int? private);

  FederationIngestResultDropped exercise(int? exercise);

  FederationIngestResultDropped expired(int? expired);

  FederationIngestResultDropped selection(int? selection);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FederationIngestResultDropped(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FederationIngestResultDropped(...).copyWith(id: 12, name: "My name")
  /// ```
  FederationIngestResultDropped call({
    int? loop,
    int? hops,
    int? dedup,
    int? licence,
    int? private,
    int? exercise,
    int? expired,
    int? selection,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFederationIngestResultDropped.copyWith(...)` or call `instanceOfFederationIngestResultDropped.copyWith.fieldName(value)` for a single field.
class _$FederationIngestResultDroppedCWProxyImpl
    implements _$FederationIngestResultDroppedCWProxy {
  const _$FederationIngestResultDroppedCWProxyImpl(this._value);

  final FederationIngestResultDropped _value;

  @override
  FederationIngestResultDropped loop(int? loop) => call(loop: loop);

  @override
  FederationIngestResultDropped hops(int? hops) => call(hops: hops);

  @override
  FederationIngestResultDropped dedup(int? dedup) => call(dedup: dedup);

  @override
  FederationIngestResultDropped licence(int? licence) => call(licence: licence);

  @override
  FederationIngestResultDropped private(int? private) => call(private: private);

  @override
  FederationIngestResultDropped exercise(int? exercise) =>
      call(exercise: exercise);

  @override
  FederationIngestResultDropped expired(int? expired) => call(expired: expired);

  @override
  FederationIngestResultDropped selection(int? selection) =>
      call(selection: selection);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FederationIngestResultDropped(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FederationIngestResultDropped(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FederationIngestResultDropped call({
    Object? loop = const $CopyWithPlaceholder(),
    Object? hops = const $CopyWithPlaceholder(),
    Object? dedup = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
    Object? private = const $CopyWithPlaceholder(),
    Object? exercise = const $CopyWithPlaceholder(),
    Object? expired = const $CopyWithPlaceholder(),
    Object? selection = const $CopyWithPlaceholder(),
  }) {
    return FederationIngestResultDropped(
      loop: loop == const $CopyWithPlaceholder()
          ? _value.loop
          // ignore: cast_nullable_to_non_nullable
          : loop as int?,
      hops: hops == const $CopyWithPlaceholder()
          ? _value.hops
          // ignore: cast_nullable_to_non_nullable
          : hops as int?,
      dedup: dedup == const $CopyWithPlaceholder()
          ? _value.dedup
          // ignore: cast_nullable_to_non_nullable
          : dedup as int?,
      licence: licence == const $CopyWithPlaceholder()
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as int?,
      private: private == const $CopyWithPlaceholder()
          ? _value.private
          // ignore: cast_nullable_to_non_nullable
          : private as int?,
      exercise: exercise == const $CopyWithPlaceholder()
          ? _value.exercise
          // ignore: cast_nullable_to_non_nullable
          : exercise as int?,
      expired: expired == const $CopyWithPlaceholder()
          ? _value.expired
          // ignore: cast_nullable_to_non_nullable
          : expired as int?,
      selection: selection == const $CopyWithPlaceholder()
          ? _value.selection
          // ignore: cast_nullable_to_non_nullable
          : selection as int?,
    );
  }
}

extension $FederationIngestResultDroppedCopyWith
    on FederationIngestResultDropped {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFederationIngestResultDropped.copyWith(...)` or `instanceOfFederationIngestResultDropped.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FederationIngestResultDroppedCWProxy get copyWith =>
      _$FederationIngestResultDroppedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FederationIngestResultDropped _$FederationIngestResultDroppedFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('FederationIngestResultDropped', json, ($checkedConvert) {
  final val = FederationIngestResultDropped(
    loop: $checkedConvert('loop', (v) => (v as num?)?.toInt()),
    hops: $checkedConvert('hops', (v) => (v as num?)?.toInt()),
    dedup: $checkedConvert('dedup', (v) => (v as num?)?.toInt()),
    licence: $checkedConvert('licence', (v) => (v as num?)?.toInt()),
    private: $checkedConvert('private', (v) => (v as num?)?.toInt()),
    exercise: $checkedConvert('exercise', (v) => (v as num?)?.toInt()),
    expired: $checkedConvert('expired', (v) => (v as num?)?.toInt()),
    selection: $checkedConvert('selection', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$FederationIngestResultDroppedToJson(
  FederationIngestResultDropped instance,
) => <String, dynamic>{
  'loop': ?instance.loop,
  'hops': ?instance.hops,
  'dedup': ?instance.dedup,
  'licence': ?instance.licence,
  'private': ?instance.private,
  'exercise': ?instance.exercise,
  'expired': ?instance.expired,
  'selection': ?instance.selection,
};

// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'reference_status.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReferenceStatusCWProxy {
  ReferenceStatus datasets(List<ReferenceDataset> datasets);

  ReferenceStatus loading(bool? loading);

  ReferenceStatus attributions(List<String>? attributions);

  ReferenceStatus boundaryNote(String? boundaryNote);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReferenceStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReferenceStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  ReferenceStatus call({
    List<ReferenceDataset> datasets,
    bool? loading,
    List<String>? attributions,
    String? boundaryNote,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReferenceStatus.copyWith(...)` or call `instanceOfReferenceStatus.copyWith.fieldName(value)` for a single field.
class _$ReferenceStatusCWProxyImpl implements _$ReferenceStatusCWProxy {
  const _$ReferenceStatusCWProxyImpl(this._value);

  final ReferenceStatus _value;

  @override
  ReferenceStatus datasets(List<ReferenceDataset> datasets) =>
      call(datasets: datasets);

  @override
  ReferenceStatus loading(bool? loading) => call(loading: loading);

  @override
  ReferenceStatus attributions(List<String>? attributions) =>
      call(attributions: attributions);

  @override
  ReferenceStatus boundaryNote(String? boundaryNote) =>
      call(boundaryNote: boundaryNote);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReferenceStatus(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReferenceStatus(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReferenceStatus call({
    Object? datasets = const $CopyWithPlaceholder(),
    Object? loading = const $CopyWithPlaceholder(),
    Object? attributions = const $CopyWithPlaceholder(),
    Object? boundaryNote = const $CopyWithPlaceholder(),
  }) {
    return ReferenceStatus(
      datasets: datasets == const $CopyWithPlaceholder() || datasets == null
          ? _value.datasets
          // ignore: cast_nullable_to_non_nullable
          : datasets as List<ReferenceDataset>,
      loading: loading == const $CopyWithPlaceholder()
          ? _value.loading
          // ignore: cast_nullable_to_non_nullable
          : loading as bool?,
      attributions: attributions == const $CopyWithPlaceholder()
          ? _value.attributions
          // ignore: cast_nullable_to_non_nullable
          : attributions as List<String>?,
      boundaryNote: boundaryNote == const $CopyWithPlaceholder()
          ? _value.boundaryNote
          // ignore: cast_nullable_to_non_nullable
          : boundaryNote as String?,
    );
  }
}

extension $ReferenceStatusCopyWith on ReferenceStatus {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReferenceStatus.copyWith(...)` or `instanceOfReferenceStatus.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReferenceStatusCWProxy get copyWith => _$ReferenceStatusCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReferenceStatus _$ReferenceStatusFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReferenceStatus', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['datasets']);
      final val = ReferenceStatus(
        datasets: $checkedConvert(
          'datasets',
          (v) => (v as List<dynamic>)
              .map((e) => ReferenceDataset.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        loading: $checkedConvert('loading', (v) => v as bool?),
        attributions: $checkedConvert(
          'attributions',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        boundaryNote: $checkedConvert('boundaryNote', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ReferenceStatusToJson(ReferenceStatus instance) =>
    <String, dynamic>{
      'datasets': instance.datasets.map((e) => e.toJson()).toList(),
      'loading': ?instance.loading,
      'attributions': ?instance.attributions,
      'boundaryNote': ?instance.boundaryNote,
    };

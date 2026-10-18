// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'reference_dataset.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReferenceDatasetCWProxy {
  ReferenceDataset name(String name);

  ReferenceDataset edition(String? edition);

  ReferenceDataset url(String? url);

  ReferenceDataset sha256(String? sha256);

  ReferenceDataset licence(String? licence);

  ReferenceDataset attribution(String? attribution);

  ReferenceDataset rows(int? rows);

  ReferenceDataset loadedAt(DateTime? loadedAt);

  ReferenceDataset optional(bool? optional);

  ReferenceDataset state(ReferenceDatasetStateEnum state);

  ReferenceDataset error(String? error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReferenceDataset(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReferenceDataset(...).copyWith(id: 12, name: "My name")
  /// ```
  ReferenceDataset call({
    String name,
    String? edition,
    String? url,
    String? sha256,
    String? licence,
    String? attribution,
    int? rows,
    DateTime? loadedAt,
    bool? optional,
    ReferenceDatasetStateEnum state,
    String? error,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfReferenceDataset.copyWith(...)` or call `instanceOfReferenceDataset.copyWith.fieldName(value)` for a single field.
class _$ReferenceDatasetCWProxyImpl implements _$ReferenceDatasetCWProxy {
  const _$ReferenceDatasetCWProxyImpl(this._value);

  final ReferenceDataset _value;

  @override
  ReferenceDataset name(String name) => call(name: name);

  @override
  ReferenceDataset edition(String? edition) => call(edition: edition);

  @override
  ReferenceDataset url(String? url) => call(url: url);

  @override
  ReferenceDataset sha256(String? sha256) => call(sha256: sha256);

  @override
  ReferenceDataset licence(String? licence) => call(licence: licence);

  @override
  ReferenceDataset attribution(String? attribution) =>
      call(attribution: attribution);

  @override
  ReferenceDataset rows(int? rows) => call(rows: rows);

  @override
  ReferenceDataset loadedAt(DateTime? loadedAt) => call(loadedAt: loadedAt);

  @override
  ReferenceDataset optional(bool? optional) => call(optional: optional);

  @override
  ReferenceDataset state(ReferenceDatasetStateEnum state) => call(state: state);

  @override
  ReferenceDataset error(String? error) => call(error: error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ReferenceDataset(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ReferenceDataset(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ReferenceDataset call({
    Object? name = const $CopyWithPlaceholder(),
    Object? edition = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? sha256 = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? rows = const $CopyWithPlaceholder(),
    Object? loadedAt = const $CopyWithPlaceholder(),
    Object? optional = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
  }) {
    return ReferenceDataset(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      edition: edition == const $CopyWithPlaceholder()
          ? _value.edition
          // ignore: cast_nullable_to_non_nullable
          : edition as String?,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      sha256: sha256 == const $CopyWithPlaceholder()
          ? _value.sha256
          // ignore: cast_nullable_to_non_nullable
          : sha256 as String?,
      licence: licence == const $CopyWithPlaceholder()
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as String?,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as String?,
      rows: rows == const $CopyWithPlaceholder()
          ? _value.rows
          // ignore: cast_nullable_to_non_nullable
          : rows as int?,
      loadedAt: loadedAt == const $CopyWithPlaceholder()
          ? _value.loadedAt
          // ignore: cast_nullable_to_non_nullable
          : loadedAt as DateTime?,
      optional: optional == const $CopyWithPlaceholder()
          ? _value.optional
          // ignore: cast_nullable_to_non_nullable
          : optional as bool?,
      state: state == const $CopyWithPlaceholder() || state == null
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as ReferenceDatasetStateEnum,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
    );
  }
}

extension $ReferenceDatasetCopyWith on ReferenceDataset {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfReferenceDataset.copyWith(...)` or `instanceOfReferenceDataset.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReferenceDatasetCWProxy get copyWith => _$ReferenceDatasetCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReferenceDataset _$ReferenceDatasetFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ReferenceDataset', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['name', 'state']);
      final val = ReferenceDataset(
        name: $checkedConvert('name', (v) => v as String),
        edition: $checkedConvert('edition', (v) => v as String?),
        url: $checkedConvert('url', (v) => v as String?),
        sha256: $checkedConvert('sha256', (v) => v as String?),
        licence: $checkedConvert('licence', (v) => v as String?),
        attribution: $checkedConvert('attribution', (v) => v as String?),
        rows: $checkedConvert('rows', (v) => (v as num?)?.toInt()),
        loadedAt: $checkedConvert(
          'loadedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        optional: $checkedConvert('optional', (v) => v as bool?),
        state: $checkedConvert(
          'state',
          (v) => $enumDecode(_$ReferenceDatasetStateEnumEnumMap, v),
        ),
        error: $checkedConvert('error', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ReferenceDatasetToJson(ReferenceDataset instance) =>
    <String, dynamic>{
      'name': instance.name,
      'edition': ?instance.edition,
      'url': ?instance.url,
      'sha256': ?instance.sha256,
      'licence': ?instance.licence,
      'attribution': ?instance.attribution,
      'rows': ?instance.rows,
      'loadedAt': ?instance.loadedAt?.toIso8601String(),
      'optional': ?instance.optional,
      'state': _$ReferenceDatasetStateEnumEnumMap[instance.state]!,
      'error': ?instance.error,
    };

const _$ReferenceDatasetStateEnumEnumMap = {
  ReferenceDatasetStateEnum.absent: 'absent',
  ReferenceDatasetStateEnum.loading: 'loading',
  ReferenceDatasetStateEnum.loaded: 'loaded',
  ReferenceDatasetStateEnum.failed: 'failed',
};

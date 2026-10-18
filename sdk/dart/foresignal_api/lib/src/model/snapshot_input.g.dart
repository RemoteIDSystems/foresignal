// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'snapshot_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SnapshotInputCWProxy {
  SnapshotInput name(String? name);

  SnapshotInput atSeq(int? atSeq);

  SnapshotInput asOf(DateTime? asOf);

  SnapshotInput sources(List<String>? sources);

  SnapshotInput kinds(List<String>? kinds);

  SnapshotInput bbox(List<num>? bbox);

  SnapshotInput includePrivate(bool? includePrivate);

  SnapshotInput personalUse(bool? personalUse);

  SnapshotInput expiresAt(DateTime? expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SnapshotInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SnapshotInput(...).copyWith(id: 12, name: "My name")
  /// ```
  SnapshotInput call({
    String? name,
    int? atSeq,
    DateTime? asOf,
    List<String>? sources,
    List<String>? kinds,
    List<num>? bbox,
    bool? includePrivate,
    bool? personalUse,
    DateTime? expiresAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSnapshotInput.copyWith(...)` or call `instanceOfSnapshotInput.copyWith.fieldName(value)` for a single field.
class _$SnapshotInputCWProxyImpl implements _$SnapshotInputCWProxy {
  const _$SnapshotInputCWProxyImpl(this._value);

  final SnapshotInput _value;

  @override
  SnapshotInput name(String? name) => call(name: name);

  @override
  SnapshotInput atSeq(int? atSeq) => call(atSeq: atSeq);

  @override
  SnapshotInput asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  SnapshotInput sources(List<String>? sources) => call(sources: sources);

  @override
  SnapshotInput kinds(List<String>? kinds) => call(kinds: kinds);

  @override
  SnapshotInput bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  SnapshotInput includePrivate(bool? includePrivate) =>
      call(includePrivate: includePrivate);

  @override
  SnapshotInput personalUse(bool? personalUse) =>
      call(personalUse: personalUse);

  @override
  SnapshotInput expiresAt(DateTime? expiresAt) => call(expiresAt: expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SnapshotInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SnapshotInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SnapshotInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? atSeq = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
    Object? kinds = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? includePrivate = const $CopyWithPlaceholder(),
    Object? personalUse = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
  }) {
    return SnapshotInput(
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      atSeq: atSeq == const $CopyWithPlaceholder()
          ? _value.atSeq
          // ignore: cast_nullable_to_non_nullable
          : atSeq as int?,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>?,
      kinds: kinds == const $CopyWithPlaceholder()
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
      includePrivate: includePrivate == const $CopyWithPlaceholder()
          ? _value.includePrivate
          // ignore: cast_nullable_to_non_nullable
          : includePrivate as bool?,
      personalUse: personalUse == const $CopyWithPlaceholder()
          ? _value.personalUse
          // ignore: cast_nullable_to_non_nullable
          : personalUse as bool?,
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
    );
  }
}

extension $SnapshotInputCopyWith on SnapshotInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSnapshotInput.copyWith(...)` or `instanceOfSnapshotInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SnapshotInputCWProxy get copyWith => _$SnapshotInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SnapshotInput _$SnapshotInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SnapshotInput', json, ($checkedConvert) {
      final val = SnapshotInput(
        name: $checkedConvert('name', (v) => v as String?),
        atSeq: $checkedConvert('atSeq', (v) => (v as num?)?.toInt()),
        asOf: $checkedConvert(
          'asOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        sources: $checkedConvert(
          'sources',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        kinds: $checkedConvert(
          'kinds',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        bbox: $checkedConvert(
          'bbox',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
        includePrivate: $checkedConvert(
          'includePrivate',
          (v) => v as bool? ?? false,
        ),
        personalUse: $checkedConvert('personalUse', (v) => v as bool? ?? false),
        expiresAt: $checkedConvert(
          'expiresAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SnapshotInputToJson(SnapshotInput instance) =>
    <String, dynamic>{
      'name': ?instance.name,
      'atSeq': ?instance.atSeq,
      'asOf': ?instance.asOf?.toIso8601String(),
      'sources': ?instance.sources,
      'kinds': ?instance.kinds,
      'bbox': ?instance.bbox,
      'includePrivate': ?instance.includePrivate,
      'personalUse': ?instance.personalUse,
      'expiresAt': ?instance.expiresAt?.toIso8601String(),
    };

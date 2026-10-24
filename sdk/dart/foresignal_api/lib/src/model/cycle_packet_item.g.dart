// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cycle_packet_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CyclePacketItemCWProxy {
  CyclePacketItem ref(String ref);

  CyclePacketItem title(String title);

  CyclePacketItem change(String change);

  CyclePacketItem severity(String? severity);

  CyclePacketItem at(DateTime? at);

  CyclePacketItem detail(Map<String, Object>? detail);

  CyclePacketItem rank(int? rank);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CyclePacketItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CyclePacketItem(...).copyWith(id: 12, name: "My name")
  /// ```
  CyclePacketItem call({
    String ref,
    String title,
    String change,
    String? severity,
    DateTime? at,
    Map<String, Object>? detail,
    int? rank,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCyclePacketItem.copyWith(...)` or call `instanceOfCyclePacketItem.copyWith.fieldName(value)` for a single field.
class _$CyclePacketItemCWProxyImpl implements _$CyclePacketItemCWProxy {
  const _$CyclePacketItemCWProxyImpl(this._value);

  final CyclePacketItem _value;

  @override
  CyclePacketItem ref(String ref) => call(ref: ref);

  @override
  CyclePacketItem title(String title) => call(title: title);

  @override
  CyclePacketItem change(String change) => call(change: change);

  @override
  CyclePacketItem severity(String? severity) => call(severity: severity);

  @override
  CyclePacketItem at(DateTime? at) => call(at: at);

  @override
  CyclePacketItem detail(Map<String, Object>? detail) => call(detail: detail);

  @override
  CyclePacketItem rank(int? rank) => call(rank: rank);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CyclePacketItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CyclePacketItem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CyclePacketItem call({
    Object? ref = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? change = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? detail = const $CopyWithPlaceholder(),
    Object? rank = const $CopyWithPlaceholder(),
  }) {
    return CyclePacketItem(
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      change: change == const $CopyWithPlaceholder() || change == null
          ? _value.change
          // ignore: cast_nullable_to_non_nullable
          : change as String,
      severity: severity == const $CopyWithPlaceholder()
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as String?,
      at: at == const $CopyWithPlaceholder()
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime?,
      detail: detail == const $CopyWithPlaceholder()
          ? _value.detail
          // ignore: cast_nullable_to_non_nullable
          : detail as Map<String, Object>?,
      rank: rank == const $CopyWithPlaceholder()
          ? _value.rank
          // ignore: cast_nullable_to_non_nullable
          : rank as int?,
    );
  }
}

extension $CyclePacketItemCopyWith on CyclePacketItem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCyclePacketItem.copyWith(...)` or `instanceOfCyclePacketItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CyclePacketItemCWProxy get copyWith => _$CyclePacketItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CyclePacketItem _$CyclePacketItemFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CyclePacketItem', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['ref', 'title', 'change']);
      final val = CyclePacketItem(
        ref: $checkedConvert('ref', (v) => v as String),
        title: $checkedConvert('title', (v) => v as String),
        change: $checkedConvert('change', (v) => v as String),
        severity: $checkedConvert('severity', (v) => v as String?),
        at: $checkedConvert(
          'at',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        detail: $checkedConvert(
          'detail',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        rank: $checkedConvert('rank', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$CyclePacketItemToJson(CyclePacketItem instance) =>
    <String, dynamic>{
      'ref': instance.ref,
      'title': instance.title,
      'change': instance.change,
      'severity': ?instance.severity,
      'at': ?instance.at?.toIso8601String(),
      'detail': ?instance.detail,
      'rank': ?instance.rank,
    };

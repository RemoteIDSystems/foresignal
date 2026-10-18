// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_frame_index_frames_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleFrameIndexFramesInnerCWProxy {
  ChronicleFrameIndexFramesInner t(DateTime t);

  ChronicleFrameIndexFramesInner counts(Map<String, int> counts);

  ChronicleFrameIndexFramesInner changed(List<String>? changed);

  ChronicleFrameIndexFramesInner changedTotal(int? changedTotal);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleFrameIndexFramesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleFrameIndexFramesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleFrameIndexFramesInner call({
    DateTime t,
    Map<String, int> counts,
    List<String>? changed,
    int? changedTotal,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleFrameIndexFramesInner.copyWith(...)` or call `instanceOfChronicleFrameIndexFramesInner.copyWith.fieldName(value)` for a single field.
class _$ChronicleFrameIndexFramesInnerCWProxyImpl
    implements _$ChronicleFrameIndexFramesInnerCWProxy {
  const _$ChronicleFrameIndexFramesInnerCWProxyImpl(this._value);

  final ChronicleFrameIndexFramesInner _value;

  @override
  ChronicleFrameIndexFramesInner t(DateTime t) => call(t: t);

  @override
  ChronicleFrameIndexFramesInner counts(Map<String, int> counts) =>
      call(counts: counts);

  @override
  ChronicleFrameIndexFramesInner changed(List<String>? changed) =>
      call(changed: changed);

  @override
  ChronicleFrameIndexFramesInner changedTotal(int? changedTotal) =>
      call(changedTotal: changedTotal);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleFrameIndexFramesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleFrameIndexFramesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleFrameIndexFramesInner call({
    Object? t = const $CopyWithPlaceholder(),
    Object? counts = const $CopyWithPlaceholder(),
    Object? changed = const $CopyWithPlaceholder(),
    Object? changedTotal = const $CopyWithPlaceholder(),
  }) {
    return ChronicleFrameIndexFramesInner(
      t: t == const $CopyWithPlaceholder() || t == null
          ? _value.t
          // ignore: cast_nullable_to_non_nullable
          : t as DateTime,
      counts: counts == const $CopyWithPlaceholder() || counts == null
          ? _value.counts
          // ignore: cast_nullable_to_non_nullable
          : counts as Map<String, int>,
      changed: changed == const $CopyWithPlaceholder()
          ? _value.changed
          // ignore: cast_nullable_to_non_nullable
          : changed as List<String>?,
      changedTotal: changedTotal == const $CopyWithPlaceholder()
          ? _value.changedTotal
          // ignore: cast_nullable_to_non_nullable
          : changedTotal as int?,
    );
  }
}

extension $ChronicleFrameIndexFramesInnerCopyWith
    on ChronicleFrameIndexFramesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleFrameIndexFramesInner.copyWith(...)` or `instanceOfChronicleFrameIndexFramesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleFrameIndexFramesInnerCWProxy get copyWith =>
      _$ChronicleFrameIndexFramesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleFrameIndexFramesInner _$ChronicleFrameIndexFramesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChronicleFrameIndexFramesInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['t', 'counts']);
  final val = ChronicleFrameIndexFramesInner(
    t: $checkedConvert('t', (v) => DateTime.parse(v as String)),
    counts: $checkedConvert('counts', (v) => Map<String, int>.from(v as Map)),
    changed: $checkedConvert(
      'changed',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    changedTotal: $checkedConvert('changedTotal', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$ChronicleFrameIndexFramesInnerToJson(
  ChronicleFrameIndexFramesInner instance,
) => <String, dynamic>{
  't': instance.t.toIso8601String(),
  'counts': instance.counts,
  'changed': ?instance.changed,
  'changedTotal': ?instance.changedTotal,
};

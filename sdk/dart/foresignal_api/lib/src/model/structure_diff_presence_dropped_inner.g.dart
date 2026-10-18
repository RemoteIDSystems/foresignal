// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'structure_diff_presence_dropped_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StructureDiffPresenceDroppedInnerCWProxy {
  StructureDiffPresenceDroppedInner path(String path);

  StructureDiffPresenceDroppedInner from(num from);

  StructureDiffPresenceDroppedInner to(num to);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StructureDiffPresenceDroppedInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StructureDiffPresenceDroppedInner(...).copyWith(id: 12, name: "My name")
  /// ```
  StructureDiffPresenceDroppedInner call({String path, num from, num to});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStructureDiffPresenceDroppedInner.copyWith(...)` or call `instanceOfStructureDiffPresenceDroppedInner.copyWith.fieldName(value)` for a single field.
class _$StructureDiffPresenceDroppedInnerCWProxyImpl
    implements _$StructureDiffPresenceDroppedInnerCWProxy {
  const _$StructureDiffPresenceDroppedInnerCWProxyImpl(this._value);

  final StructureDiffPresenceDroppedInner _value;

  @override
  StructureDiffPresenceDroppedInner path(String path) => call(path: path);

  @override
  StructureDiffPresenceDroppedInner from(num from) => call(from: from);

  @override
  StructureDiffPresenceDroppedInner to(num to) => call(to: to);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StructureDiffPresenceDroppedInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StructureDiffPresenceDroppedInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StructureDiffPresenceDroppedInner call({
    Object? path = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
  }) {
    return StructureDiffPresenceDroppedInner(
      path: path == const $CopyWithPlaceholder() || path == null
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String,
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as num,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as num,
    );
  }
}

extension $StructureDiffPresenceDroppedInnerCopyWith
    on StructureDiffPresenceDroppedInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStructureDiffPresenceDroppedInner.copyWith(...)` or `instanceOfStructureDiffPresenceDroppedInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StructureDiffPresenceDroppedInnerCWProxy get copyWith =>
      _$StructureDiffPresenceDroppedInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StructureDiffPresenceDroppedInner _$StructureDiffPresenceDroppedInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StructureDiffPresenceDroppedInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['path', 'from', 'to']);
  final val = StructureDiffPresenceDroppedInner(
    path: $checkedConvert('path', (v) => v as String),
    from: $checkedConvert('from', (v) => v as num),
    to: $checkedConvert('to', (v) => v as num),
  );
  return val;
});

Map<String, dynamic> _$StructureDiffPresenceDroppedInnerToJson(
  StructureDiffPresenceDroppedInner instance,
) => <String, dynamic>{
  'path': instance.path,
  'from': instance.from,
  'to': instance.to,
};

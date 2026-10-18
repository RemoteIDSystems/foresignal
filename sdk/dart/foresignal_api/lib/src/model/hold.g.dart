// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hold.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HoldCWProxy {
  Hold id(String id);

  Hold name(String name);

  Hold reason(String reason);

  Hold sources(List<String>? sources);

  Hold kinds(List<String>? kinds);

  Hold from(DateTime? from);

  Hold to(DateTime? to);

  Hold fromSeq(int? fromSeq);

  Hold toSeq(int? toSeq);

  Hold expiresAt(DateTime? expiresAt);

  Hold createdAt(DateTime createdAt);

  Hold createdBy(String? createdBy);

  Hold heldRows(int heldRows);

  Hold heldBytes(int heldBytes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Hold(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Hold(...).copyWith(id: 12, name: "My name")
  /// ```
  Hold call({
    String id,
    String name,
    String reason,
    List<String>? sources,
    List<String>? kinds,
    DateTime? from,
    DateTime? to,
    int? fromSeq,
    int? toSeq,
    DateTime? expiresAt,
    DateTime createdAt,
    String? createdBy,
    int heldRows,
    int heldBytes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHold.copyWith(...)` or call `instanceOfHold.copyWith.fieldName(value)` for a single field.
class _$HoldCWProxyImpl implements _$HoldCWProxy {
  const _$HoldCWProxyImpl(this._value);

  final Hold _value;

  @override
  Hold id(String id) => call(id: id);

  @override
  Hold name(String name) => call(name: name);

  @override
  Hold reason(String reason) => call(reason: reason);

  @override
  Hold sources(List<String>? sources) => call(sources: sources);

  @override
  Hold kinds(List<String>? kinds) => call(kinds: kinds);

  @override
  Hold from(DateTime? from) => call(from: from);

  @override
  Hold to(DateTime? to) => call(to: to);

  @override
  Hold fromSeq(int? fromSeq) => call(fromSeq: fromSeq);

  @override
  Hold toSeq(int? toSeq) => call(toSeq: toSeq);

  @override
  Hold expiresAt(DateTime? expiresAt) => call(expiresAt: expiresAt);

  @override
  Hold createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Hold createdBy(String? createdBy) => call(createdBy: createdBy);

  @override
  Hold heldRows(int heldRows) => call(heldRows: heldRows);

  @override
  Hold heldBytes(int heldBytes) => call(heldBytes: heldBytes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Hold(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Hold(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Hold call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? reason = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
    Object? kinds = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? fromSeq = const $CopyWithPlaceholder(),
    Object? toSeq = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? createdBy = const $CopyWithPlaceholder(),
    Object? heldRows = const $CopyWithPlaceholder(),
    Object? heldBytes = const $CopyWithPlaceholder(),
  }) {
    return Hold(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      reason: reason == const $CopyWithPlaceholder() || reason == null
          ? _value.reason
          // ignore: cast_nullable_to_non_nullable
          : reason as String,
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>?,
      kinds: kinds == const $CopyWithPlaceholder()
          ? _value.kinds
          // ignore: cast_nullable_to_non_nullable
          : kinds as List<String>?,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
      fromSeq: fromSeq == const $CopyWithPlaceholder()
          ? _value.fromSeq
          // ignore: cast_nullable_to_non_nullable
          : fromSeq as int?,
      toSeq: toSeq == const $CopyWithPlaceholder()
          ? _value.toSeq
          // ignore: cast_nullable_to_non_nullable
          : toSeq as int?,
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      createdBy: createdBy == const $CopyWithPlaceholder()
          ? _value.createdBy
          // ignore: cast_nullable_to_non_nullable
          : createdBy as String?,
      heldRows: heldRows == const $CopyWithPlaceholder() || heldRows == null
          ? _value.heldRows
          // ignore: cast_nullable_to_non_nullable
          : heldRows as int,
      heldBytes: heldBytes == const $CopyWithPlaceholder() || heldBytes == null
          ? _value.heldBytes
          // ignore: cast_nullable_to_non_nullable
          : heldBytes as int,
    );
  }
}

extension $HoldCopyWith on Hold {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHold.copyWith(...)` or `instanceOfHold.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HoldCWProxy get copyWith => _$HoldCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Hold _$HoldFromJson(Map<String, dynamic> json) => $checkedCreate('Hold', json, (
  $checkedConvert,
) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'name',
      'reason',
      'createdAt',
      'heldRows',
      'heldBytes',
    ],
  );
  final val = Hold(
    id: $checkedConvert('id', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String),
    reason: $checkedConvert('reason', (v) => v as String),
    sources: $checkedConvert(
      'sources',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    kinds: $checkedConvert(
      'kinds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    from: $checkedConvert(
      'from',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    to: $checkedConvert(
      'to',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    fromSeq: $checkedConvert('fromSeq', (v) => (v as num?)?.toInt()),
    toSeq: $checkedConvert('toSeq', (v) => (v as num?)?.toInt()),
    expiresAt: $checkedConvert(
      'expiresAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    createdBy: $checkedConvert('createdBy', (v) => v as String?),
    heldRows: $checkedConvert('heldRows', (v) => (v as num).toInt()),
    heldBytes: $checkedConvert('heldBytes', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$HoldToJson(Hold instance) => <String, dynamic>{
  'id': instance.id,
  'name': instance.name,
  'reason': instance.reason,
  'sources': ?instance.sources,
  'kinds': ?instance.kinds,
  'from': ?instance.from?.toIso8601String(),
  'to': ?instance.to?.toIso8601String(),
  'fromSeq': ?instance.fromSeq,
  'toSeq': ?instance.toSeq,
  'expiresAt': ?instance.expiresAt?.toIso8601String(),
  'createdAt': instance.createdAt.toIso8601String(),
  'createdBy': ?instance.createdBy,
  'heldRows': instance.heldRows,
  'heldBytes': instance.heldBytes,
};

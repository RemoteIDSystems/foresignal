// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'feed_key_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FeedKeyInputCWProxy {
  FeedKeyInput name(String name);

  FeedKeyInput prefixes(List<String> prefixes);

  FeedKeyInput restrictions(ReadRestriction restrictions);

  FeedKeyInput expiresAt(DateTime expiresAt);

  FeedKeyInput privateAccess(bool? privateAccess);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FeedKeyInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FeedKeyInput(...).copyWith(id: 12, name: "My name")
  /// ```
  FeedKeyInput call({
    String name,
    List<String> prefixes,
    ReadRestriction restrictions,
    DateTime expiresAt,
    bool? privateAccess,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFeedKeyInput.copyWith(...)` or call `instanceOfFeedKeyInput.copyWith.fieldName(value)` for a single field.
class _$FeedKeyInputCWProxyImpl implements _$FeedKeyInputCWProxy {
  const _$FeedKeyInputCWProxyImpl(this._value);

  final FeedKeyInput _value;

  @override
  FeedKeyInput name(String name) => call(name: name);

  @override
  FeedKeyInput prefixes(List<String> prefixes) => call(prefixes: prefixes);

  @override
  FeedKeyInput restrictions(ReadRestriction restrictions) =>
      call(restrictions: restrictions);

  @override
  FeedKeyInput expiresAt(DateTime expiresAt) => call(expiresAt: expiresAt);

  @override
  FeedKeyInput privateAccess(bool? privateAccess) =>
      call(privateAccess: privateAccess);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FeedKeyInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FeedKeyInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FeedKeyInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? prefixes = const $CopyWithPlaceholder(),
    Object? restrictions = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? privateAccess = const $CopyWithPlaceholder(),
  }) {
    return FeedKeyInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      prefixes: prefixes == const $CopyWithPlaceholder() || prefixes == null
          ? _value.prefixes
          // ignore: cast_nullable_to_non_nullable
          : prefixes as List<String>,
      restrictions:
          restrictions == const $CopyWithPlaceholder() || restrictions == null
          ? _value.restrictions
          // ignore: cast_nullable_to_non_nullable
          : restrictions as ReadRestriction,
      expiresAt: expiresAt == const $CopyWithPlaceholder() || expiresAt == null
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime,
      privateAccess: privateAccess == const $CopyWithPlaceholder()
          ? _value.privateAccess
          // ignore: cast_nullable_to_non_nullable
          : privateAccess as bool?,
    );
  }
}

extension $FeedKeyInputCopyWith on FeedKeyInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFeedKeyInput.copyWith(...)` or `instanceOfFeedKeyInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FeedKeyInputCWProxy get copyWith => _$FeedKeyInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FeedKeyInput _$FeedKeyInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('FeedKeyInput', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['name', 'prefixes', 'restrictions', 'expiresAt'],
  );
  final val = FeedKeyInput(
    name: $checkedConvert('name', (v) => v as String),
    prefixes: $checkedConvert(
      'prefixes',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    restrictions: $checkedConvert(
      'restrictions',
      (v) => ReadRestriction.fromJson(v as Map<String, dynamic>),
    ),
    expiresAt: $checkedConvert('expiresAt', (v) => DateTime.parse(v as String)),
    privateAccess: $checkedConvert('privateAccess', (v) => v as bool? ?? false),
  );
  return val;
});

Map<String, dynamic> _$FeedKeyInputToJson(FeedKeyInput instance) =>
    <String, dynamic>{
      'name': instance.name,
      'prefixes': instance.prefixes,
      'restrictions': instance.restrictions.toJson(),
      'expiresAt': instance.expiresAt.toIso8601String(),
      'privateAccess': ?instance.privateAccess,
    };

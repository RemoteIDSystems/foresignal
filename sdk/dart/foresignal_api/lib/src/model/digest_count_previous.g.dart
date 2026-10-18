// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'digest_count_previous.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DigestCountPreviousCWProxy {
  DigestCountPrevious new_(int? new_);

  DigestCountPrevious changed(int? changed);

  DigestCountPrevious removed(int? removed);

  DigestCountPrevious active(int? active);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestCountPrevious(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestCountPrevious(...).copyWith(id: 12, name: "My name")
  /// ```
  DigestCountPrevious call({
    int? new_,
    int? changed,
    int? removed,
    int? active,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDigestCountPrevious.copyWith(...)` or call `instanceOfDigestCountPrevious.copyWith.fieldName(value)` for a single field.
class _$DigestCountPreviousCWProxyImpl implements _$DigestCountPreviousCWProxy {
  const _$DigestCountPreviousCWProxyImpl(this._value);

  final DigestCountPrevious _value;

  @override
  DigestCountPrevious new_(int? new_) => call(new_: new_);

  @override
  DigestCountPrevious changed(int? changed) => call(changed: changed);

  @override
  DigestCountPrevious removed(int? removed) => call(removed: removed);

  @override
  DigestCountPrevious active(int? active) => call(active: active);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestCountPrevious(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestCountPrevious(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DigestCountPrevious call({
    Object? new_ = const $CopyWithPlaceholder(),
    Object? changed = const $CopyWithPlaceholder(),
    Object? removed = const $CopyWithPlaceholder(),
    Object? active = const $CopyWithPlaceholder(),
  }) {
    return DigestCountPrevious(
      new_: new_ == const $CopyWithPlaceholder()
          ? _value.new_
          // ignore: cast_nullable_to_non_nullable
          : new_ as int?,
      changed: changed == const $CopyWithPlaceholder()
          ? _value.changed
          // ignore: cast_nullable_to_non_nullable
          : changed as int?,
      removed: removed == const $CopyWithPlaceholder()
          ? _value.removed
          // ignore: cast_nullable_to_non_nullable
          : removed as int?,
      active: active == const $CopyWithPlaceholder()
          ? _value.active
          // ignore: cast_nullable_to_non_nullable
          : active as int?,
    );
  }
}

extension $DigestCountPreviousCopyWith on DigestCountPrevious {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDigestCountPrevious.copyWith(...)` or `instanceOfDigestCountPrevious.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DigestCountPreviousCWProxy get copyWith =>
      _$DigestCountPreviousCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DigestCountPrevious _$DigestCountPreviousFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DigestCountPrevious', json, ($checkedConvert) {
      final val = DigestCountPrevious(
        new_: $checkedConvert('new', (v) => (v as num?)?.toInt()),
        changed: $checkedConvert('changed', (v) => (v as num?)?.toInt()),
        removed: $checkedConvert('removed', (v) => (v as num?)?.toInt()),
        active: $checkedConvert('active', (v) => (v as num?)?.toInt()),
      );
      return val;
    }, fieldKeyMap: const {'new_': 'new'});

Map<String, dynamic> _$DigestCountPreviousToJson(
  DigestCountPrevious instance,
) => <String, dynamic>{
  'new': ?instance.new_,
  'changed': ?instance.changed,
  'removed': ?instance.removed,
  'active': ?instance.active,
};

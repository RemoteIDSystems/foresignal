// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_snapshot.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpSnapshotCWProxy {
  EdpSnapshot id(String id);

  EdpSnapshot stream(String stream);

  EdpSnapshot scope(String? scope);

  EdpSnapshot page(int page);

  EdpSnapshot final_(bool final_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpSnapshot(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpSnapshot(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpSnapshot call({
    String id,
    String stream,
    String? scope,
    int page,
    bool final_,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpSnapshot.copyWith(...)` or call `instanceOfEdpSnapshot.copyWith.fieldName(value)` for a single field.
class _$EdpSnapshotCWProxyImpl implements _$EdpSnapshotCWProxy {
  const _$EdpSnapshotCWProxyImpl(this._value);

  final EdpSnapshot _value;

  @override
  EdpSnapshot id(String id) => call(id: id);

  @override
  EdpSnapshot stream(String stream) => call(stream: stream);

  @override
  EdpSnapshot scope(String? scope) => call(scope: scope);

  @override
  EdpSnapshot page(int page) => call(page: page);

  @override
  EdpSnapshot final_(bool final_) => call(final_: final_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpSnapshot(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpSnapshot(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpSnapshot call({
    Object? id = const $CopyWithPlaceholder(),
    Object? stream = const $CopyWithPlaceholder(),
    Object? scope = const $CopyWithPlaceholder(),
    Object? page = const $CopyWithPlaceholder(),
    Object? final_ = const $CopyWithPlaceholder(),
  }) {
    return EdpSnapshot(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      stream: stream == const $CopyWithPlaceholder() || stream == null
          ? _value.stream
          // ignore: cast_nullable_to_non_nullable
          : stream as String,
      scope: scope == const $CopyWithPlaceholder()
          ? _value.scope
          // ignore: cast_nullable_to_non_nullable
          : scope as String?,
      page: page == const $CopyWithPlaceholder() || page == null
          ? _value.page
          // ignore: cast_nullable_to_non_nullable
          : page as int,
      final_: final_ == const $CopyWithPlaceholder() || final_ == null
          ? _value.final_
          // ignore: cast_nullable_to_non_nullable
          : final_ as bool,
    );
  }
}

extension $EdpSnapshotCopyWith on EdpSnapshot {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpSnapshot.copyWith(...)` or `instanceOfEdpSnapshot.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpSnapshotCWProxy get copyWith => _$EdpSnapshotCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpSnapshot _$EdpSnapshotFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpSnapshot', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'stream', 'page', 'final']);
      final val = EdpSnapshot(
        id: $checkedConvert('id', (v) => v as String),
        stream: $checkedConvert('stream', (v) => v as String),
        scope: $checkedConvert('scope', (v) => v as String?),
        page: $checkedConvert('page', (v) => (v as num).toInt()),
        final_: $checkedConvert('final', (v) => v as bool),
      );
      return val;
    }, fieldKeyMap: const {'final_': 'final'});

Map<String, dynamic> _$EdpSnapshotToJson(EdpSnapshot instance) =>
    <String, dynamic>{
      'id': instance.id,
      'stream': instance.stream,
      'scope': ?instance.scope,
      'page': instance.page,
      'final': instance.final_,
    };

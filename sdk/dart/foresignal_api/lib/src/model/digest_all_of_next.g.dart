// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'digest_all_of_next.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DigestAllOfNextCWProxy {
  DigestAllOfNext sinceSeq(int sinceSeq);

  DigestAllOfNext sinceAlertId(int? sinceAlertId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestAllOfNext(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestAllOfNext(...).copyWith(id: 12, name: "My name")
  /// ```
  DigestAllOfNext call({int sinceSeq, int? sinceAlertId});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDigestAllOfNext.copyWith(...)` or call `instanceOfDigestAllOfNext.copyWith.fieldName(value)` for a single field.
class _$DigestAllOfNextCWProxyImpl implements _$DigestAllOfNextCWProxy {
  const _$DigestAllOfNextCWProxyImpl(this._value);

  final DigestAllOfNext _value;

  @override
  DigestAllOfNext sinceSeq(int sinceSeq) => call(sinceSeq: sinceSeq);

  @override
  DigestAllOfNext sinceAlertId(int? sinceAlertId) =>
      call(sinceAlertId: sinceAlertId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestAllOfNext(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestAllOfNext(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DigestAllOfNext call({
    Object? sinceSeq = const $CopyWithPlaceholder(),
    Object? sinceAlertId = const $CopyWithPlaceholder(),
  }) {
    return DigestAllOfNext(
      sinceSeq: sinceSeq == const $CopyWithPlaceholder() || sinceSeq == null
          ? _value.sinceSeq
          // ignore: cast_nullable_to_non_nullable
          : sinceSeq as int,
      sinceAlertId: sinceAlertId == const $CopyWithPlaceholder()
          ? _value.sinceAlertId
          // ignore: cast_nullable_to_non_nullable
          : sinceAlertId as int?,
    );
  }
}

extension $DigestAllOfNextCopyWith on DigestAllOfNext {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDigestAllOfNext.copyWith(...)` or `instanceOfDigestAllOfNext.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DigestAllOfNextCWProxy get copyWith => _$DigestAllOfNextCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DigestAllOfNext _$DigestAllOfNextFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DigestAllOfNext', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['sinceSeq']);
      final val = DigestAllOfNext(
        sinceSeq: $checkedConvert('sinceSeq', (v) => (v as num).toInt()),
        sinceAlertId: $checkedConvert(
          'sinceAlertId',
          (v) => (v as num?)?.toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$DigestAllOfNextToJson(DigestAllOfNext instance) =>
    <String, dynamic>{
      'sinceSeq': instance.sinceSeq,
      'sinceAlertId': ?instance.sinceAlertId,
    };

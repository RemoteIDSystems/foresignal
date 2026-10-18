// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'alert_context_anchor.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AlertContextAnchorCWProxy {
  AlertContextAnchor source_(String? source_);

  AlertContextAnchor externalId(String? externalId);

  AlertContextAnchor seq(int? seq);

  AlertContextAnchor distanceKm(num? distanceKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertContextAnchor(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertContextAnchor(...).copyWith(id: 12, name: "My name")
  /// ```
  AlertContextAnchor call({
    String? source_,
    String? externalId,
    int? seq,
    num? distanceKm,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAlertContextAnchor.copyWith(...)` or call `instanceOfAlertContextAnchor.copyWith.fieldName(value)` for a single field.
class _$AlertContextAnchorCWProxyImpl implements _$AlertContextAnchorCWProxy {
  const _$AlertContextAnchorCWProxyImpl(this._value);

  final AlertContextAnchor _value;

  @override
  AlertContextAnchor source_(String? source_) => call(source_: source_);

  @override
  AlertContextAnchor externalId(String? externalId) =>
      call(externalId: externalId);

  @override
  AlertContextAnchor seq(int? seq) => call(seq: seq);

  @override
  AlertContextAnchor distanceKm(num? distanceKm) =>
      call(distanceKm: distanceKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertContextAnchor(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertContextAnchor(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AlertContextAnchor call({
    Object? source_ = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? seq = const $CopyWithPlaceholder(),
    Object? distanceKm = const $CopyWithPlaceholder(),
  }) {
    return AlertContextAnchor(
      source_: source_ == const $CopyWithPlaceholder()
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as String?,
      externalId: externalId == const $CopyWithPlaceholder()
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String?,
      seq: seq == const $CopyWithPlaceholder()
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int?,
      distanceKm: distanceKm == const $CopyWithPlaceholder()
          ? _value.distanceKm
          // ignore: cast_nullable_to_non_nullable
          : distanceKm as num?,
    );
  }
}

extension $AlertContextAnchorCopyWith on AlertContextAnchor {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAlertContextAnchor.copyWith(...)` or `instanceOfAlertContextAnchor.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AlertContextAnchorCWProxy get copyWith =>
      _$AlertContextAnchorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AlertContextAnchor _$AlertContextAnchorFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AlertContextAnchor', json, ($checkedConvert) {
      final val = AlertContextAnchor(
        source_: $checkedConvert('source', (v) => v as String?),
        externalId: $checkedConvert('externalId', (v) => v as String?),
        seq: $checkedConvert('seq', (v) => (v as num?)?.toInt()),
        distanceKm: $checkedConvert('distanceKm', (v) => v as num?),
      );
      return val;
    }, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$AlertContextAnchorToJson(AlertContextAnchor instance) =>
    <String, dynamic>{
      'source': ?instance.source_,
      'externalId': ?instance.externalId,
      'seq': ?instance.seq,
      'distanceKm': ?instance.distanceKm,
    };

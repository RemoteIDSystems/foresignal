// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'edp_manifest.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EdpManifestCWProxy {
  EdpManifest protocol(String protocol);

  EdpManifest source_(EdpSourceInfo source_);

  EdpManifest transport(EdpTransport transport);

  EdpManifest streams(List<EdpStream> streams);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpManifest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpManifest(...).copyWith(id: 12, name: "My name")
  /// ```
  EdpManifest call({
    String protocol,
    EdpSourceInfo source_,
    EdpTransport transport,
    List<EdpStream> streams,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEdpManifest.copyWith(...)` or call `instanceOfEdpManifest.copyWith.fieldName(value)` for a single field.
class _$EdpManifestCWProxyImpl implements _$EdpManifestCWProxy {
  const _$EdpManifestCWProxyImpl(this._value);

  final EdpManifest _value;

  @override
  EdpManifest protocol(String protocol) => call(protocol: protocol);

  @override
  EdpManifest source_(EdpSourceInfo source_) => call(source_: source_);

  @override
  EdpManifest transport(EdpTransport transport) => call(transport: transport);

  @override
  EdpManifest streams(List<EdpStream> streams) => call(streams: streams);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EdpManifest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EdpManifest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EdpManifest call({
    Object? protocol = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
    Object? transport = const $CopyWithPlaceholder(),
    Object? streams = const $CopyWithPlaceholder(),
  }) {
    return EdpManifest(
      protocol: protocol == const $CopyWithPlaceholder() || protocol == null
          ? _value.protocol
          // ignore: cast_nullable_to_non_nullable
          : protocol as String,
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as EdpSourceInfo,
      transport: transport == const $CopyWithPlaceholder() || transport == null
          ? _value.transport
          // ignore: cast_nullable_to_non_nullable
          : transport as EdpTransport,
      streams: streams == const $CopyWithPlaceholder() || streams == null
          ? _value.streams
          // ignore: cast_nullable_to_non_nullable
          : streams as List<EdpStream>,
    );
  }
}

extension $EdpManifestCopyWith on EdpManifest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEdpManifest.copyWith(...)` or `instanceOfEdpManifest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EdpManifestCWProxy get copyWith => _$EdpManifestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EdpManifest _$EdpManifestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EdpManifest', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['protocol', 'source', 'transport', 'streams'],
      );
      final val = EdpManifest(
        protocol: $checkedConvert('protocol', (v) => v as String),
        source_: $checkedConvert(
          'source',
          (v) => EdpSourceInfo.fromJson(v as Map<String, dynamic>),
        ),
        transport: $checkedConvert(
          'transport',
          (v) => EdpTransport.fromJson(v as Map<String, dynamic>),
        ),
        streams: $checkedConvert(
          'streams',
          (v) => (v as List<dynamic>)
              .map((e) => EdpStream.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    }, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$EdpManifestToJson(EdpManifest instance) =>
    <String, dynamic>{
      'protocol': instance.protocol,
      'source': instance.source_.toJson(),
      'transport': instance.transport.toJson(),
      'streams': instance.streams.map((e) => e.toJson()).toList(),
    };

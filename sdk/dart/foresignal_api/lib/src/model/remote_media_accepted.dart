//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'remote_media_accepted.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RemoteMediaAccepted {
  /// Returns a new [RemoteMediaAccepted] instance.
  RemoteMediaAccepted({

    required  this.mediaId,

    required  this.sha256,

     this.passId,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'mediaId',
    required: true,
    includeIfNull: false,
  )


  final String mediaId;



  @JsonKey(
    
    name: r'sha256',
    required: true,
    includeIfNull: false,
  )


  final String sha256;



      /// Pass the image attached to, or the minted pass
  @JsonKey(
    
    name: r'passId',
    required: false,
    includeIfNull: false,
  )


  final String? passId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RemoteMediaAccepted &&
      other.mediaId == mediaId &&
      other.sha256 == sha256 &&
      other.passId == passId;

    @override
    int get hashCode =>
        mediaId.hashCode +
        sha256.hashCode +
        (passId == null ? 0 : passId.hashCode);

  factory RemoteMediaAccepted.fromJson(Map<String, dynamic> json) => _$RemoteMediaAcceptedFromJson(json);

  Map<String, dynamic> toJson() => _$RemoteMediaAcceptedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}


//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_related_page_items_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_link_found_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleLinkFoundEvent {
  /// Returns a new [ChronicleLinkFoundEvent] instance.
  ChronicleLinkFoundEvent({

    required  this.chronicleId,

    required  this.link,

    required  this.at,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'chronicleId',
    required: true,
    includeIfNull: false,
  )


  final String chronicleId;



  @JsonKey(
    
    name: r'link',
    required: true,
    includeIfNull: false,
  )


  final ChronicleRelatedPageItemsInner link;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleLinkFoundEvent &&
      other.chronicleId == chronicleId &&
      other.link == link &&
      other.at == at;

    @override
    int get hashCode =>
        chronicleId.hashCode +
        link.hashCode +
        at.hashCode;

  factory ChronicleLinkFoundEvent.fromJson(Map<String, dynamic> json) => _$ChronicleLinkFoundEventFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleLinkFoundEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

